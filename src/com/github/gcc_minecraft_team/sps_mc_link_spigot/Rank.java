package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;

public class Rank implements ConfigurationSerializable {
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

    // Serialization
    private static String NAMEKEY = "name";
    private static String PERMSKEY = "perms";
    private static String COLORKEY = "color";

    public Rank(Map<String, Object> map) {
        this((String) map.get(NAMEKEY));
        this.perms = (HashMap<String, Boolean>) map.get(PERMSKEY);
        // Try and get the color from the string; default to white if not recognized.
        try {
            this.color = ChatColor.valueOf(((String) map.get(COLORKEY)).strip().replace(" ", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.color = ChatColor.WHITE;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(NAMEKEY, this.name);
        map.put(PERMSKEY, this.perms);
        map.put(COLORKEY, this.color.name());
        return map;
    }
}
