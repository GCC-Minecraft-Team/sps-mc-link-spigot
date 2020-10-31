package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;

import java.util.HashMap;

public class Rank {
    /**
     * This is not for differentiating between members and unlinked players.
     * Ranks are flairs, mod tags, etc.
     * They are additional on top of member status.
     * */

    private String name;
    private HashMap<String, Boolean> perms;
    private ChatColor color;

    public Rank(String name) {
        this.name = name;
        this.perms = new HashMap<>();
        this.color = ChatColor.WHITE;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasPermission(String perm) {
        return perms.containsKey(perm) && perms.get(perm) == true;
    }

    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    public void setPermission(String perm, boolean value) {
        perms.put(perm, value);
    }

    public void setPermission(Permission perm, boolean value) {
        setPermission(perm.getName(), value);
    }

    public void unsetPermission(String perm) {
        perms.remove(perm);
    }

    public void unsetPermission(Permission perm) {
        unsetPermission(perm.getName());
    }

    public HashMap<String, Boolean> getPerms() {
        return perms;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }
}
