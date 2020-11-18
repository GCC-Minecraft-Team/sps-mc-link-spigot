package com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
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
    private int extraClaims;
    private final HashMap<Permission, Boolean> perms;
    private ChatColor color;

    public Rank(String name) {
        this.name = name;
        this.perms = new HashMap<>();
        this.color = ChatColor.WHITE;
    }

    // ========[NAME]========

    /**
     * Getter for the {@link Rank}'s name.
     * @return This {@link Rank}'s name.
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    // ========[RANK PERKS]========

    /**
     * Gets the amount of extra claims this rank has
     * @return the number of extra claim chunks
     */
    @NotNull
    public int getExtraClaims() { return this.extraClaims; }

    /**
     * Sets the amount of extra claims this rank should have.
     * @param extraClaims the number of extra claim chunks
     */
    public void setExtraClaims(int extraClaims) {
        this.extraClaims = extraClaims;
        SPSSpigot.perms().saveFile();
    }

    // ========[PERMISSIONS]========

    /**
     * Checks whether or not this {@link Rank} has a {@link Permission} node set to {@code true}.
     * @param perm The {@link Permission} node to check.
     * @return The value of the {@link Permission} if set to for this {@link Rank}; {@code false} if unset.
     */
    public boolean hasPermission(@NotNull Permission perm) {
        return perms.containsKey(perm) && perms.get(perm);
    }

    /**
     * Checks whether or not this {@link Rank} has a permission node set to {@code true}.
     * @param perm Fully qualified name of the {@link Permission} to check.
     * @return The value of the permission if set for this {@link Rank}; {@code false} if unset.
     */
    public boolean hasPermission(@NotNull String perm) {
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            return hasPermission(permObj);
        else
            return false;
    }
    /**
     * Sets the value of a {@link Permission} node for this {@link Rank}.
     * @param perm The {@link Permission} node to set the value of.
     * @param value The value to set.
     */
    public void setPermission(@NotNull Permission perm, boolean value) {
        perms.put(perm, value);
        SPSSpigot.perms().saveFile();
        updateRankHolders();
    }

    /**
     * Sets the value of a permission node for this {@link Rank}.
     * @param perm The fully qualified name of the permission node to set the value of.
     * @param value The value to set.
     */
    public void setPermission(@NotNull String perm, boolean value) {
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            setPermission(permObj, value);
    }

    /**
     * Unsets a {@link Permission} node for this {@link Rank}. This removes the node from the list, effectively making the {@link Rank} neither give nor actively deny the {@link Permission}.
     * @param perm The {@link Permission} node to unset.
     */
    public void unsetPermission(@NotNull Permission perm) {
        perms.remove(perm);
        SPSSpigot.perms().saveFile();
        updateRankHolders();
    }

    /**
     * Unsets a permission node for this {@link Rank}. This removes the node from the list, effectively making the {@link Rank} neither give nor actively deny the permission.
     * @param perm The fully qualified name of the permission node to unset.
     */
    public void unsetPermission(@NotNull String perm) {
        Permission permObj = SPSSpigot.server().getPluginManager().getPermission(perm);
        if (permObj != null)
            unsetPermission(permObj);
    }

    /**
     * Gets the values of all set permission nodes.
     * @return An unmodifiable {@link Map} containing the fully qualified names of each permission node and the value they are set to.
     */
    @NotNull
    public Map<Permission, Boolean> getPerms() {
        return Collections.unmodifiableMap(perms);
    }

    // ========[COLOR]========

    /**
     * Gets the color for this {@link Rank}
     * @return The color of this {@link Rank}.
     */
    @NotNull
    public ChatColor getColor() {
        return this.color;
    }

    /**
     * Sets the color for this {@link Rank}.
     * @param color The color to set.
     */
    public void setColor(@NotNull ChatColor color) {
        this.color = color;
        SPSSpigot.perms().saveFile();
    }

    // ========[RANK HOLDERS]========

    /**
     * Convenience method that calls {@link PermissionsHandler#getRankPlayers(Rank)}
     * @return A set of all UUIDs of players with this rank.
     */
    @NotNull
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

    // ========[SERIALIZATION]========

    private static final String NAMEKEY = "name";
    private static final String EXTRACLAIMSKEY = "extraclaims";
    private static final String PERMSKEY = "perms";
    private static final String COLORKEY = "color";

    public Rank(@NotNull Map<String, Object> map) {
        this((String) map.get(NAMEKEY));
        Map<String, Boolean> permStrs = (HashMap<String, Boolean>) map.get(PERMSKEY);
        for (Map.Entry<String, Boolean> permStr : permStrs.entrySet()) {
            Permission perm = SPSSpigot.server().getPluginManager().getPermission(permStr.getKey());
            if (perm != null)
                this.perms.put(perm, permStr.getValue());
        }

        // get extra claims
        if (map.get(EXTRACLAIMSKEY) != null) {
            this.extraClaims = (int) map.get(EXTRACLAIMSKEY);
        } else {
            this.extraClaims = 0;
        }

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
        Map<String, Boolean> permStrs = new HashMap<>();
        for (Map.Entry<Permission, Boolean> perm : this.perms.entrySet())
            permStrs.put(perm.getKey().getName(), perm.getValue());
        map.put(PERMSKEY, permStrs);
        map.put(COLORKEY, this.color.name());
        map.put(EXTRACLAIMSKEY, this.extraClaims);
        return map;
    }
}
