package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionsHandler {

    public static final String PERMFILE = "permsConfig.yml";
    public static final String CFGMEMBERS = "members";
    public static final String CFGRANKS = "ranks";
    public static final String CFGPLAYERS = "player_ranks";

    private Map<Player, PermissionAttachment> players;

    private Map<String, Boolean> memberPerms;

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
        permsConfig.addDefault(CFGMEMBERS, new HashMap<String, Boolean>());
        permsConfig.addDefault(CFGRANKS, new HashSet<Rank>());
        permsConfig.addDefault(CFGPLAYERS, new HashMap<OfflinePlayer, HashSet<Rank>>());
        loadFile();
        /*
        memberPerms.put("spsmc.basic.*", true);
        ranks.add(new Rank("Rank One"));
        playerRanks.put(SPSSpigot.plugin().getServer().getOfflinePlayers()[0], new HashSet<>());
        saveFile();
        */
    }

    public void saveFile() {
        permsConfig.set(CFGMEMBERS, memberPerms);
        permsConfig.set(CFGRANKS, ranks);
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

    public void loadFile() {
        try {
            try {
                permsConfig.load(new File(SPSSpigot.plugin().getDataFolder(), PERMFILE));
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            System.out.println(0);
            memberPerms = (Map<String, Boolean>) permsConfig.get(CFGMEMBERS);
            System.out.println(1);
            ranks = (Set<Rank>) permsConfig.get(CFGRANKS);
            System.out.println(2);
            // Deserialize player ranks
            Map<String, ArrayList<String>> serialPlayerRanks = (Map<String, ArrayList<String>>) permsConfig.get(CFGPLAYERS);
            System.out.println(3);
            playerRanks = new HashMap<>();
            for (Map.Entry<String, ArrayList<String>> player : serialPlayerRanks.entrySet()) {
                Set<Rank> ranks = new HashSet<>();
                for (String rank : player.getValue())
                    ranks.add(getRank(rank));
                playerRanks.put(UUID.fromString(player.getKey()), ranks);
            }
        } catch (ClassCastException e) {
            System.out.println("\n\n\n NOOOOOO \n\n\n");
        }
    }

    public boolean removeAttachment(Player player) {
        if (players.containsKey(player)) {
            PermissionAttachment attach = players.get(player);
            players.remove(player);
            return attach.remove();
        } else {
            return false;
        }
    }

    /**
     * Only finds whether or not the permission is set for members.
     */
    public boolean isMemberPermSet(String perm) {
        return memberPerms.containsKey(perm);
    }

    /**
     * Only finds whether or not the permission is set for members.
     */
    public boolean isMemberPermSet(Permission perm) {
        return isMemberPermSet(perm.getName());
    }

    /**
     * Finds what the permission is set to; throws error if unset.
     */
    public boolean getMemberPerm(String perm) {
        return memberPerms.get(perm);
    }

    /**
     * Finds what the permission is set to; throw error if unset.
     */
    public boolean getMemberPerm(Permission perm) {
        return getMemberPerm(perm.getName());
    }

    public void setMemberPerm(String perm, boolean value) {
        memberPerms.put(perm, value);
        saveFile();
        for (Player player : players.keySet())
            loadPermissions(player);
    }

    public void setMemberPerm(Permission perm, boolean value) {
        // Saving occurs in the called method.
        setMemberPerm(perm.getName(), value);
    }

    public boolean unsetMemberPerm(String perm) {
        boolean out = memberPerms.remove(perm);
        saveFile();
        for (Player player : players.keySet())
            loadPermissions(player);
        return out;
    }

    public boolean unsetMemberPerm(Permission perm) {
        // Saving occurs in the called method.
        return unsetMemberPerm(perm.getName());
    }

    public Rank getRank(String name) {
        for (Rank r : ranks) {
            if (name.equalsIgnoreCase(r.getName()))
                return r;
        }
        return null;
    }

    public Set<Rank> getRanks() {
        return ranks;
    }

    public void addRank(Rank rank) {
        if (getRank(rank.getName()) == null)
            ranks.add(rank);
        saveFile();
    }

    public boolean deleteRank(Rank rank) {
        for (Map.Entry<UUID, Set<Rank>> rankSet : playerRanks.entrySet()) {
            rankSet.getValue().remove(rank);
            if (SPSSpigot.plugin().getServer().getOfflinePlayer(rankSet.getKey()).isOnline())
                loadPermissions(SPSSpigot.plugin().getServer().getOfflinePlayer(rankSet.getKey()).getPlayer());
        }
        boolean out = ranks.remove(rank);
        saveFile();
        return out;
    }

    public void deleteRank(String name) {
        for (Map.Entry<UUID, Set<Rank>> rankSet : playerRanks.entrySet()) {
            ArrayList<Rank> remove = new ArrayList<>();
            for (Rank r : rankSet.getValue()) {
                if (r.getName().equals(name))
                    remove.add(r);
            }
            for (Rank r : remove) {
                rankSet.getValue().remove(r);
            }
            if (SPSSpigot.plugin().getServer().getOfflinePlayer(rankSet.getKey()).isOnline())
                loadPermissions(SPSSpigot.plugin().getServer().getOfflinePlayer(rankSet.getKey()).getPlayer());
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

    public Set<Rank> getPlayerRanks(OfflinePlayer player) {
        if (playerRanks.containsKey(player.getUniqueId()))
            return playerRanks.get(player.getUniqueId());
        else
            return new HashSet<>();
    }

    public Set<UUID> getRankPlayers(Rank rank) {
        Set<UUID> uuids = new HashSet<>();
        for (Map.Entry<UUID, Set<Rank>> player : playerRanks.entrySet())
            if (player.getValue().contains(rank))
                uuids.add(player.getKey());
        return uuids;
    }

    public boolean hasPlayerRank(OfflinePlayer player, Rank rank) {
        return playerRanks.containsKey(player.getUniqueId()) && playerRanks.get(player.getUniqueId()).contains(rank);
    }

    public boolean hasPlayerRank(OfflinePlayer player, String rank) {
        Rank rObj = getRank(rank);
        return rObj != null && hasPlayerRank(player, rObj);
    }

    public boolean givePlayerRank(OfflinePlayer player, Rank rank) {
        if (!playerRanks.containsKey(player.getUniqueId()))
            playerRanks.put(player.getUniqueId(), new HashSet<>());
        boolean out = playerRanks.get(player.getUniqueId()).add(rank);
        saveFile();
        if (player.isOnline())
            loadPermissions(player.getPlayer());
        return out;
    }

    public boolean givePlayerRank(OfflinePlayer player, String rank) {
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
        // Saving occurs in the called method.
        return givePlayerRank(player, rankObj);
    }

    public boolean removePlayerRank(OfflinePlayer player, Rank rank) {
        if (!playerRanks.containsKey(player.getUniqueId())) {
            return false;
        } else {
            boolean out = playerRanks.get(player.getUniqueId()).remove(rank);
            saveFile();
            if (player.isOnline())
                loadPermissions(player.getPlayer());
            return out;
        }
    }

    public boolean removePlayerRank(OfflinePlayer player, String rank) {
        // Doesn't work if there is a rank with the same name but is different in this player's list. This should never happen.
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
        // Saving occurs within the called method.
        return removePlayerRank(player, rankObj);
    }

    public void loadPermissions(Player player) {
        if (players.containsKey(player)) {
            players.get(player).remove();
        }
        PermissionAttachment a = player.addAttachment(SPSSpigot.plugin());
        // Remove all perms for unlinked players or add member perms for linked players
        if (!DatabaseLink.isRegistered(player.getUniqueId()))
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions())
                a.setPermission(perm.getPermission(), false);
        else
            for (Map.Entry<String, Boolean> perm : memberPerms.entrySet())
                a.setPermission(perm.getKey(), perm.getValue());
        // Add perms for each rank
        for (Rank rank : getPlayerRanks(player))
            for (Map.Entry<String, Boolean> perm : rank.getPerms().entrySet())
                a.setPermission(perm.getKey(), perm.getValue());

        players.put(player, a);
        player.recalculatePermissions();
        for (PermissionAttachmentInfo i : player.getEffectivePermissions()) {
            System.out.println(i.getPermission() + " - " + i.getValue());
        }
    }

}
