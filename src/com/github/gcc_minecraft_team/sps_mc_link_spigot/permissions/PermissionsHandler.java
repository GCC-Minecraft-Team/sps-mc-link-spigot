package com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionsHandler {

    private static final String PERMFILE = "permsConfig.yml";
    private static final String CFGMEMBERS = "members";
    private static final String CFGRANKS = "ranks";
    private static final String CFGPLAYERS = "player_ranks";

    private Map<Player, PermissionAttachment> players;

    private Map<Permission, Boolean> memberPerms;

    private Set<Rank> ranks;
    private Map<UUID, Set<Rank>> playerRanks;

    private FileConfiguration permsConfig;

    public PermissionsHandler() {
        players = new HashMap<>();
        memberPerms = new HashMap<>();
        ranks = new HashSet<>();
        playerRanks = new HashMap<>();

        SPSSpigot.plugin().saveResource(PERMFILE, false);
        permsConfig = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), PERMFILE));
        loadFile();
    }

    /**
     * Saves the data in this {@link PermissionsHandler} to {@value PERMFILE}.
     */
    public void saveFile() {
        // Save members
        Map<String, Boolean> strMemPerms = new HashMap<>();
        for (Map.Entry<Permission, Boolean> perm : memberPerms.entrySet())
            strMemPerms.put(perm.getKey().getName(), perm.getValue());
        permsConfig.set(CFGMEMBERS, strMemPerms);
        // Save ranks
        permsConfig.set(CFGRANKS, new ArrayList<>(ranks));
        // Serialize player ranks to only store UUID String and rank names rather than their entire objects
        HashMap<String, ArrayList<String>> serialPlayerRanks = new HashMap<>();
        for (Map.Entry<UUID, Set<Rank>> player : playerRanks.entrySet()) {
            ArrayList<String> rankNames = new ArrayList<>();
            for (Rank rank : player.getValue())
                rankNames.add(rank.getName());
            serialPlayerRanks.put(player.getKey().toString(), rankNames);
        }
        permsConfig.set(CFGPLAYERS, serialPlayerRanks);
        try {
            permsConfig.save(new File(SPSSpigot.plugin().getDataFolder(), PERMFILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads data for this {@link PermissionsHandler} from {@value PERMFILE}.
     */
    public void loadFile() {
        try {
            permsConfig.load(new File(SPSSpigot.plugin().getDataFolder(), PERMFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        // Deserialize member perms
        memberPerms = new HashMap<>();
        MemorySection objMembers = (MemorySection) permsConfig.get(CFGMEMBERS);
        for (String perm : objMembers.getKeys(false)) {
            Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
            if (permObj != null)
                memberPerms.put(permObj, objMembers.getBoolean(perm));
        }
        // Deserialize ranks
        ranks = new HashSet<>((List<Rank>) permsConfig.getList(CFGRANKS));
        // Deserialize player ranks
        playerRanks = new HashMap<>();
        MemorySection objPlayers = (MemorySection) permsConfig.get(CFGPLAYERS);
        Map<String, List<String>> serialPlayerRanks = new HashMap<>();
        for (String player : objPlayers.getKeys(false))
            serialPlayerRanks.put(player, objPlayers.getStringList(player));
        // Parse player ranks
        playerRanks = new HashMap<>();
        for (Map.Entry<String, List<String>> player : serialPlayerRanks.entrySet()) {
            Set<Rank> ranks = new HashSet<>();
            for (String rank : player.getValue())
                ranks.add(getRank(rank));
            playerRanks.put(UUID.fromString(player.getKey()), ranks);
        }
    }

    /**
     * Deletes the {@link PermissionsHandler} for the given {@link Player}. This generally should only be called when the {@link Player} is leaves the server.
     * @param player The {@link Player} whose {@link PermissionsHandler} should be deleted.
     * @return {@code true} if something was changed ({@code false} just means there was nothing to delete)
     */
    public boolean removeAttachment(@NotNull Player player) {
        if (players.containsKey(player)) {
            PermissionAttachment attach = players.get(player);
            players.remove(player);
            return attach.remove();
        } else {
            return false;
        }
    }

    /**
     * Only finds whether or not a permission is set for members.
     * @param perm The {@link Permission} node to check.
     * @return {@code true} if the {@link Permission} node is set for members.
     */
    public boolean isMemberPermSet(@NotNull Permission perm) {
        return memberPerms.containsKey(perm);
    }

    /**
     * Only finds whether or not a permission is set for members.
     * @param perm The fully qualified name of the permission node to check.
     * @return {@code true} if the permission node is set for members.
     */
    public boolean isMemberPermSet(@NotNull String perm) {
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            return isMemberPermSet(perm);
        else
            return false;
    }

    /**
     * Finds what a given {@link Permission} is set to for members.
     * @param perm The {@link Permission} node to check.
     * @return The value of the {@link Permission} node.
     * @throws NullPointerException If the {@link Permission} node is unset.
     */
    public boolean getMemberPerm(@NotNull Permission perm) {
        return memberPerms.get(perm);
    }

    /**
     * Finds what a given permission is set to for members.
     * @param perm The fully qualified name of the permission node to check.
     * @return The value of the permission node.
     * @throws NullPointerException If the permission node is unset.
     */
    public boolean getMemberPerm(@NotNull String perm) {
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            return getMemberPerm(permObj);
        else
            return false;
    }

    /**
     * Gets the values of all set permission nodes for members.
     * @return A map containing the fully qualified names of each permission node and the value they are set to.
     */
    @NotNull
    public Map<Permission, Boolean> getMemberPerms() {
        return Collections.unmodifiableMap(memberPerms);
    }

    /**
     * Sets the value of a given {@link Permission} node.
     * @param perm The {@link Permission} node to be set.
     * @param value The value to set.
     */
    public void setMemberPerm(@NotNull Permission perm, boolean value) {
        memberPerms.put(perm, value);
        saveFile();
        for (Player player : SPSSpigot.server().getOnlinePlayers())
            loadPermissions(player);
    }

    /**
     * Sets the value of a given permission node.
     * @param perm The fully qualified name of the permission node to be set.
     * @param value The value to set.
     */
    public void setMemberPerm(@NotNull String perm, boolean value) {
        // Saving occurs in the called method.
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            setMemberPerm(permObj, value);
    }

    /**
     * Unsets a given {@link Permission} node. This removes the node from the list, effectively making membership neither give nor deny the {@link Permission}.
     * @param perm The {@link Permission} node to unset.
     * @return {@code true} if something was changed ({@code false} means the {@link Permission} node was already unset).
     */
    public boolean unsetMemberPerm(@NotNull Permission perm) {
        boolean out = memberPerms.remove(perm);
        saveFile();
        for (Player player : SPSSpigot.server().getOnlinePlayers())
            loadPermissions(player);
        return out;
    }

    /**
     * Unsets a given permission node. This removes the node from the list, effectively making membership neither give nor deny the permission.
     * @param perm The fully qualified name of the permission node to unset.
     * @return {@code true} if something was changed ({@code false} means the permission node was already unset).
     */
    public boolean unsetMemberPerm(@NotNull String perm) {
        // Saving occurs in the called method.
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            return unsetMemberPerm(permObj);
        else
            return false;
    }

    /**
     * Gets a {@link Rank} by its name.
     * @param name The name to search for.
     * @return The {@link Rank} with the given name, or {@code null} if no {@link Rank} is found by that name.
     */
    @Nullable
    public Rank getRank(@NotNull String name) {
        for (Rank r : ranks) {
            if (name.equalsIgnoreCase(r.getName()))
                return r;
        }
        return null;
    }

    /**
     * Gets all {@link Rank}s known by this {@link PermissionsHandler}.
     * @return An unmodifiable {@link Set} of all {@link Rank}s.
     */
    @NotNull
    public Set<Rank> getRanks() {
        return Collections.unmodifiableSet(ranks);
    }

    /**
     * Gets the names of all {@link Rank}s known by this {@link PermissionsHandler}.
     * @return A {@link Set} of the names of all known {@link Rank}s.
     */
    @NotNull
    public Set<String> getRankNames() {
        Set<String> names = new HashSet<>();
        for (Rank rank : getRanks())
            names.add(rank.getName());
        return names;
    }

    /**
     * Adds a new {@link Rank} to this {@link PermissionsHandler}
     * @param rank The {@link Rank} to add.
     */
    public void addRank(@NotNull Rank rank) {
        if (getRank(rank.getName()) == null)
            ranks.add(rank);
        saveFile();
    }

    /**
     * Deletes a {@link Rank} from this {@link PermissionsHandler} and all players, both online and offline.
     * @param rank The {@link Rank} to delete.
     * @return {@code true} if this {@link PermissionsHandler} was changed. Ignores changes to individual players (they should have the same result).
     */
    public boolean deleteRank(@NotNull Rank rank) {
        for (Map.Entry<UUID, Set<Rank>> rankSet : playerRanks.entrySet()) {
            rankSet.getValue().remove(rank);
            Player player = SPSSpigot.server().getPlayer(rankSet.getKey());
            if (player != null)
                loadPermissions(player);
        }
        boolean out = ranks.remove(rank);
        saveFile();
        return out;
    }

    /**
     * Deletes a {@link Rank} from this {@link PermissionsHandler} and for all players, both online and offline, based on the name.
     * @param name The name of the {@link Rank} to be deleted.
     */
    public void deleteRank(@NotNull String name) {
        for (Map.Entry<UUID, Set<Rank>> rankSet : playerRanks.entrySet()) {
            ArrayList<Rank> remove = new ArrayList<>();
            for (Rank r : rankSet.getValue()) {
                if (r.getName().equals(name))
                    remove.add(r);
            }
            for (Rank r : remove) {
                rankSet.getValue().remove(r);
            }
            Player player = SPSSpigot.server().getPlayer(rankSet.getKey());
            if (player != null)
                loadPermissions(player);
        }
        ArrayList<Rank> remove = new ArrayList<>();
        for (Rank r : ranks) {
            if (r.getName().equals(name))
                remove.add(r);
        }
        for (Rank r : remove)
            ranks.remove(r);
        saveFile();
    }

    /**
     * Gets all the {@link Rank}s held by a given player.
     * @param player The {@link UUID} of the player to check.
     * @return An unmodifiable {@link Set} of {@link Rank}s held by the player.
     */
    @NotNull
    public Set<Rank> getPlayerRanks(@NotNull UUID player) {
        if (playerRanks.containsKey(player))
            return Collections.unmodifiableSet(playerRanks.get(player));
        else
            return new HashSet<>();
    }

    /**
     * Get all the players that hold a given {@link Rank}.
     * @param rank The {@link Rank} to check.
     * @return A {@link Set} of the {@link UUID}s of the players that hold the {@link Rank}.
     */
    @NotNull
    public Set<UUID> getRankPlayers(@NotNull Rank rank) {
        Set<UUID> uuids = new HashSet<>();
        for (Map.Entry<UUID, Set<Rank>> player : playerRanks.entrySet())
            if (player.getValue().contains(rank))
                uuids.add(player.getKey());
        return uuids;
    }

    /**
     * Gets whether or not a player has a given {@link Rank}.
     * @param player The {@link UUID} of the player to check.
     * @param rank The {@link Rank} to check.
     * @return {@code true} if the given player has the given {@link Rank}.
     */
    public boolean hasPlayerRank(@NotNull UUID player, @NotNull Rank rank) {
        return playerRanks.containsKey(player) && playerRanks.get(player).contains(rank);
    }

    /**
     * Gets whether or not a player has the known {@link Rank} found by its name.
     * @param player The {@link UUID} of the player to check.
     * @param rank The name of the {@link Rank} to check.
     * @return {@code true} if the given player has the known {@link Rank} of the given name ({@code false} if no {@link Rank} exists by the given name).
     */
    public boolean hasPlayerRank(@NotNull UUID player, @NotNull String rank) {
        Rank rObj = getRank(rank);
        return rObj != null && hasPlayerRank(player, rObj);
    }

    /**
     * Gives a {@link Rank} to a specified player.
     * @param player The {@link UUID} of the player to give the {@link Rank}.
     * @param rank The {@link Rank} to give.
     * @return {@code true} if the {@link Rank} was given ({@code false} means the player already had the {@link Rank}).
     */
    public boolean givePlayerRank(@NotNull UUID player, @NotNull Rank rank) {
        if (!playerRanks.containsKey(player))
            playerRanks.put(player, new HashSet<>());
        boolean out = playerRanks.get(player).add(rank);
        saveFile();
        Player p = SPSSpigot.server().getPlayer(player);
        if (p != null)
            loadPermissions(p);
        return out;
    }

    /**
     * Gives a rank to a specified player.
     * @param player The {@link UUID} of the player to give the rank.
     * @param rank The name of the {@link Rank} to give.
     * @return {@code true} if the rank was given ({@code false} means the player already had the rank).
     * @throws IllegalArgumentException If the {@link Rank} was not found by the given name.
     */
    public boolean givePlayerRank(@NotNull UUID player, @NotNull String rank) {
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
        // Saving occurs in the called method.
        return givePlayerRank(player, rankObj);
    }

    /**
     * Removes a {@link Rank} from a specified player.
     * @param player The {@link UUID} of the player to remove the {@link Rank} from.
     * @param rank The {@link Rank} to remove.
     * @return {@code true} if the {@link Rank} was removed ({@code false} means the player already did not have the {@link Rank}).
     */
    public boolean removePlayerRank(@NotNull UUID player, @NotNull Rank rank) {
        if (!playerRanks.containsKey(player)) {
            return false;
        } else {
            boolean out = playerRanks.get(player).remove(rank);
            saveFile();
            Player p = SPSSpigot.server().getPlayer(player);
            if (p != null)
                loadPermissions(p);
            return out;
        }
    }

    /**
     * Removes a rank from a specified player.
     * @param player The {@link UUID} of the player to remove the rank from.
     * @param rank The name of the {@link Rank} to remove.
     * @return {@code true} if the rank was removed ({@code false} means the player already did not have the rank).
     * @throws IllegalArgumentException If the {@link Rank} was not found by the given name.
     */
    public boolean removePlayerRank(@NotNull UUID player, @NotNull String rank) {
        // Doesn't work if there is a rank with the same name but is different in this player's list. This should never happen.
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
        // Saving occurs within the called method.
        return removePlayerRank(player, rankObj);
    }

    /**
     * Updates the permissions for the given {@link Player}.
     * @param player The {@link Player} to update permissions for.
     */
    public void loadPermissions(@NotNull Player player) {
        if (players.containsKey(player)) {
            players.get(player).remove();
        }
        PermissionAttachment a = player.addAttachment(SPSSpigot.plugin());
        // Remove all perms for unlinked players or add member perms for linked players
        if (!DatabaseLink.isRegistered(player.getUniqueId()))
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions())
                a.setPermission(perm.getPermission(), false);
        else
            for (Map.Entry<Permission, Boolean> perm : memberPerms.entrySet())
                a.setPermission(perm.getKey(), perm.getValue());
        // Add perms for each rank
        for (Rank rank : getPlayerRanks(player.getUniqueId()))
            for (Map.Entry<Permission, Boolean> perm : rank.getPerms().entrySet())
                a.setPermission(perm.getKey(), perm.getValue());

        players.put(player, a);
        player.recalculatePermissions();
        /*
        for (PermissionAttachmentInfo i : player.getEffectivePermissions()) {
            System.out.println(i.getPermission() + " - " + i.getValue());
        }
        */
    }

}
