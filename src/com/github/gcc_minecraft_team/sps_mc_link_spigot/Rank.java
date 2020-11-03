package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This is not for differentiating between members and unlinked players.
 * Ranks are flairs, mod tags, etc.
 * They are additional on top of member status.
 * */
public class Rank implements ConfigurationSerializable {

    private final String name;
    private HashMap<String, Boolean> perms;
    private ChatColor color;

    public Rank(String name) {
        this.name = name;
        this.perms = new HashMap<>();
        this.color = ChatColor.WHITE;
    }

    /**
     *
     * @return This {@link Rank}'s name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Checks whether or not this {@link Rank} has a permission node set to {@code true}.
     * @param perm Fully qualified name of the {@link Permission} to check.
     * @return The value of the permission if set for this {@link Rank}; {@code false} if unset.
     */
    public boolean hasPermission(String perm) {
        return perms.containsKey(perm) && perms.get(perm);
    }

    /**
     * Checks whether or not this {@link Rank} has a {@link Permission} node set to {@code true}.
     * @param perm The {@link Permission} node to check.
     * @return The value of the {@link Permission} if set for this {@link Rank}; {@code false} if unset.
     */
    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    /**
     * Sets the value of a permission node for this {@link Rank}.
     * @param perm The fully qualified name of the permission node to set the value of.
     * @param value The value to set.
     */
    public void setPermission(String perm, boolean value) {
        perms.put(perm, value);
        SPSSpigot.perms().saveFile();
        updateRankHolders();
    }

    /**
     * Sets the value of a {@link Permission} node for this {@link Rank}.
     * @param perm The {@link Permission} node to set the value of.
     * @param value The value to set.
     */
    public void setPermission(Permission perm, boolean value) {
        setPermission(perm.getName(), value);
    }

    /**
     * Unsets a permission node for this {@link Rank}. This removes the node from the list, effectively making the {@link Rank} neither give nor actively deny the permission.
     * @param perm The fully qualified name of the permission node to unset.
     */
    public void unsetPermission(String perm) {
        perms.remove(perm);
        SPSSpigot.perms().saveFile();
        updateRankHolders();
    }

    /**
     * Unsets a {@link Permission} node for this {@link Rank}. This removes the node from the list, effectively making the {@link Rank} neither give nor actively deny the {@link Permission}.
     * @param perm The {@link Permission} node to unset.
     */
    public void unsetPermission(Permission perm) {
        unsetPermission(perm.getName());
    }

    /**
     * Gets the values of all set permission nodes.
     * @return A map containing the fully qualified names of each permission node and the value they are set to.
     */
    public Map<String, Boolean> getPerms() {
        return perms;
    }

    /**
     * Gets the color for this {@link Rank}
     * @return The color of this {@link Rank}.
     */
    public ChatColor getColor() {
        return this.color;
    }

    /**
     * Sets the color for this {@link Rank}.
     * @param color The color to set.
     */
    public void setColor(ChatColor color) {
        this.color = color;
        SPSSpigot.perms().saveFile();
    }

    /**
     * Convenience method that calls {@link PermissionsHandler#getRankPlayers(Rank)}
     * @return A set of all UUIDs of players with this rank.
     */
    public Set<UUID> getRankHolders() {
        return SPSSpigot.perms().getRankPlayers(this);
    }

    /**
     * Updates the permissions of each online {@link Player} that has this {@link Rank}.
     */
    private void updateRankHolders() {
        for (UUID uuid : getRankHolders()) {
            Player player = SPSSpigot.server().getPlayer(uuid);
            if (player != null)
                SPSSpigot.perms().loadPermissions(player);
        }
    }

    // Serialization
    private static final String NAMEKEY = "name";
    private static final String PERMSKEY = "perms";
    private static final String COLORKEY = "color";

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

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(NAMEKEY, this.name);
        map.put(PERMSKEY, this.perms);
        map.put(COLORKEY, this.color.name());
        return map;
    }
}
