package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Team {

    public String name;
    public Set<UUID> members;
    public UUID leader;

    private UUID worldGroup;

    public Team(TeamSerializable team) {
        this.name = team.name;
        this.members = team.members;
        this.leader = team.leader;
        this.worldGroup = team.WGID;
    }

    public Team(@NotNull WorldGroup worldGroup, @NotNull String name, @NotNull UUID leader) {
        this.name = name;
        this.leader = leader;
        this.worldGroup = worldGroup.getID();
        members = new HashSet<>();
        members.add(leader);
    }

    /**
     * Getter for the {@link Team}'s name.
     * @return This {@link Team}'s name.
     */
    @NotNull
    public String getName() {
        return name;
    }


    /**
     * Getter for the {@link WorldGroup} that contains this {@link Team}.
     * @return The {@link WorldGroup} containing this {@link Team}.
     */
    @NotNull
    public WorldGroup getWorldGroup() {
        return SPSSpigot.plugin().getWorldGroup(worldGroup);
    }

    /**
     * Getter for the {@link Team}'s members. READ ONLY.
     * @return An unmodifiable {@link Set} of members' {@link UUID}s.
     */
    @NotNull
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets all known members of this {@link Team} sorted alphabetically.
     * @return A sorted {@link List} of members' names.
     */
    @NotNull
    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : members)
            names.add(DatabaseLink.getSPSName(uuid));
        return names;
    }

    /**
     * Finds whether a player is a member.
     * @param player The {@link UUID} of the member to check.
     * @return {@code true} if the player is a member.
     */
    public boolean isMember(@NotNull UUID player) {
        return members.contains(player);
    }

    /**
     * Finds whether a player is a member.
     * @param player The {@link OfflinePlayer} to check.
     * @return {@code true} if the player is a member.
     */
    public boolean isMember(@NotNull OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Adds a player to the {@link Team}. Will not add if already a member of a {@link Team}.
     * @param player The {@link UUID} of the player to add.
     * @return {@code true} if successful.
     */
    public boolean addMember(@NotNull UUID player) {
        if (getWorldGroup().getPlayerTeam(player) == null) {
            boolean out = members.add(player);
            DatabaseLink.updateTeam(this);
            return out;
        } else {
            // Player is already on a team.
            return false;
        }
    }

    /**
     * Adds a player to the {@link Team}. Will not add if already a member of a {@link Team}.
     * @param player The {@link OfflinePlayer} to add.
     * @return {@code true} if successful.
     */
    public boolean addMember(@NotNull OfflinePlayer player) {
        return addMember(player.getUniqueId());
    }

    /**
     * Removes a player from the {@link Team}. The leader may not leave unless they are the last member.
     * @param player The {@link UUID} of the player to remove.
     * @return {@code true} if successful.
     */
    public boolean removeMember(@NotNull UUID player) {
        if (player.equals(leader)) {
            if (members.size() == 1) {
                // Delete the team
                getWorldGroup().deleteTeam(this);
                DatabaseLink.updateTeam(this);
                return true;
            } else {
                return false;
            }
        }
        boolean out = members.remove(player);
        DatabaseLink.updateTeam(this);
        return out;
    }

    /**
     * Removes a player from the {@link Team}. The leader may not leave unless they are the last member.
     * @param player The {@link OfflinePlayer} to remove.
     * @return {@code true} if successful.
     */
    public boolean removeMember(@NotNull OfflinePlayer player) {
        return removeMember(player.getUniqueId());
    }

    /**
     * Gets the leader.
     * @return The {@link UUID} of the leader.
     */
    @NotNull
    public UUID getLeader() {
        return leader;
    }

    /**
     * Changes the {@link Team}'s leader to another member.
     * @param newLeader The {@link UUID} of the new leader. The new leader must be a member.
     * @return {@code true} if successfully changed the leader.
     */
    public boolean changeLeader(@NotNull UUID newLeader) {
        if (isMember(newLeader)) {
            leader = newLeader;
            DatabaseLink.updateTeam(this);
            return true;
        } else {
            return false;
        }
    }
}
