package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PermissionsHandler {

    private HashMap<Player, PermissionAttachment> players;

    private HashMap<String, Boolean> memberPerms;

    private HashSet<Rank> ranks;
    private HashMap<OfflinePlayer, HashSet<Rank>> playerRanks;

    public PermissionsHandler() {
        players = new HashMap<>();
        memberPerms = new HashMap<>();
        ranks = new HashSet<>();
        playerRanks = new HashMap<>();
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
    }

    public void setMemberPerm(Permission perm, boolean value) {
        setMemberPerm(perm.getName(), value);
    }

    public boolean unsetMemberPerm(String perm) {
        return memberPerms.remove(perm);
    }

    public boolean unsetMemberPerm(Permission perm) {
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
    }

    public boolean removeRank(Rank rank) {
        for (HashSet<Rank> rankSet : playerRanks.values())
            rankSet.remove(rank);
        return ranks.remove(rank);
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
    }

    public boolean givePlayerRank(OfflinePlayer player, Rank rank) {
        if (!playerRanks.containsKey(player))
            playerRanks.put(player, new HashSet<>());
        return playerRanks.get(player).add(rank);
    }

    public boolean givePlayerRank(OfflinePlayer player, String rank) {
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
        return givePlayerRank(player, rankObj);
    }

    public boolean removePlayerRank(OfflinePlayer player, Rank rank) {
        if (!playerRanks.containsKey(player))
            return false;
        else
            return playerRanks.get(player).remove(rank);
    }

    public boolean removePlayerRank(OfflinePlayer player, String rank) {
        // Doesn't work if there is a rank with the same name but is different in this player's list. This should never happen.
        Rank rankObj = getRank(rank);
        if (rankObj == null)
            throw new IllegalArgumentException("Permissions rank '" + rank + "' was not recognized.");
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
