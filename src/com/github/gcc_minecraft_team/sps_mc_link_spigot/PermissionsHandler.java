package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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

    private HashMap<Player, PermissionAttachment> players;

    private HashMap<String, Boolean> memberPerms;

    private HashSet<Rank> ranks;
    private HashMap<OfflinePlayer, HashSet<Rank>> playerRanks;

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
        for (Map.Entry<OfflinePlayer, HashSet<Rank>> player : playerRanks.entrySet()) {
            ArrayList<String> rankNames = new ArrayList<>();
            for (Rank rank : player.getValue())
                rankNames.add(rank.getName());
            serialPlayerRanks.put(player.getKey().getUniqueId().toString(), rankNames);
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
            memberPerms = (HashMap<String, Boolean>) permsConfig.get(CFGMEMBERS);
            ranks = (HashSet<Rank>) permsConfig.get(CFGRANKS);
            // Deserialize player ranks
            HashMap<String, ArrayList<String>> serialPlayerRanks = (HashMap<String, ArrayList<String>>) permsConfig.get(CFGPLAYERS);
            playerRanks = new HashMap<>();
            for (Map.Entry<String, ArrayList<String>> player : serialPlayerRanks.entrySet()) {
                HashSet<Rank> ranks = new HashSet<>();
                for (String rank : player.getValue())
                    ranks.add(getRank(rank));
                playerRanks.put(SPSSpigot.plugin().getServer().getOfflinePlayer(UUID.fromString(player.getKey())), ranks);
            }
        } catch (ClassCastException e) {

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

    public boolean isMemberPermSet(String perm) {
        /**
         * Only finds whether or not the permission is set for members.
         */
        return memberPerms.containsKey(perm);
    }

    public boolean isMemberPermSet(Permission perm) {
        /**
         * Only finds whether or not the permission is set for members.
         */
        return isMemberPermSet(perm.getName());
    }

    public boolean getMemberPerm(String perm) {
        /**
         * Finds what the permission is set to; throws error if unset.
         */
        return memberPerms.get(perm);
    }

    public boolean getMemberPerm(Permission perm) {
        /**
         * Finds what the permission is set to; throw error if unset.
         */
        return getMemberPerm(perm.getName());
    }

    public void setMemberPerm(String perm, boolean value) {
        memberPerms.put(perm, value);
        saveFile();
    }

    public void setMemberPerm(Permission perm, boolean value) {
        // Saving occurs in the called method.
        setMemberPerm(perm.getName(), value);
    }

    public boolean unsetMemberPerm(String perm) {
        boolean out = memberPerms.remove(perm);
        saveFile();
        return out;
    }

    public boolean unsetMemberPerm(Permission perm) {
        // Saving occurs in the called method.
        return unsetMemberPerm(perm.getName());
    }

    public Rank getRank(String name) {
        for (Rank r : ranks) {
            if (name.equals(r.getName()))
                return r;
        }
        return null;
    }

    public void addRank(Rank rank) {
        if (getRank(rank.getName()) == null)
            ranks.add(rank);
        saveFile();
    }

    public boolean removeRank(Rank rank) {
        for (HashSet<Rank> rankSet : playerRanks.values())
            rankSet.remove(rank);
        boolean out = ranks.remove(rank);
        saveFile();
        return out;
    }

    public void removeRank(String name) {
        for (HashSet<Rank> rankSet : playerRanks.values()) {
            ArrayList<Rank> remove = new ArrayList<>();
            for (Rank r : rankSet) {
                if (r.getName().equals(name))
                    remove.add(r);
            }
            for (Rank r : remove) {
                rankSet.remove(r);
            }
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

    public boolean givePlayerRank(OfflinePlayer player, Rank rank) {
        if (!playerRanks.containsKey(player))
            playerRanks.put(player, new HashSet<>());
        boolean out = playerRanks.get(player).add(rank);
        saveFile();
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
        if (!playerRanks.containsKey(player)) {
            return false;
        } else {
            boolean out = playerRanks.get(player).remove(rank);
            saveFile();
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

    public HashMap<String, Boolean> generatePermissions(OfflinePlayer player, boolean linked) {
        HashMap<String, Boolean> perms = new HashMap<>();
        // Add member perms
        if (linked)
            perms.putAll(memberPerms);
        // Add rank perms
        if (playerRanks.containsKey(player)) {
            for (Rank rank : playerRanks.get(player)) {
                perms.putAll(rank.getPerms());
            }
        }
        return perms;
    }

    public void loadPermissions(Player player, boolean linked) {
        if (players.containsKey(player)) {
            players.get(player).remove();
        }
        PermissionAttachment a = player.addAttachment(SPSSpigot.plugin());
        if (!linked) {
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
                a.setPermission(perm.getPermission(), false);
            }
        }
        a.getPermissions().putAll(generatePermissions(player, linked));
        players.put(player, a);

        for (PermissionAttachmentInfo i : player.getEffectivePermissions()) {
            System.out.println(i.getPermission() + " - " + i.getValue());
        }
    }

}
