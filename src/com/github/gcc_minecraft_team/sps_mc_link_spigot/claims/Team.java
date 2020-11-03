package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Team implements ConfigurationSerializable {

    private final String name;
    private Set<UUID> members;
    private UUID leader;

    public Team(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        members.add(leader);
    }

    /**
     * Getter for the {@link Team}'s name.
     * @return This {@link Team}'s name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the {@link Team}'s members. READ ONLY.
     * @return An unmodifiable {@link Set} of members' {@link UUID}s.
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Finds whether a player is a member.
     * @param player The {@link UUID} of the member to check.
     * @return {@code true} if the player is a member.
     */
    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    /**
     * Finds whether a player is a member.
     * @param player The {@link OfflinePlayer} to check.
     * @return {@code true} if the player is a member.
     */
    public boolean isMember(OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Adds a player to the team. Will not add if already a member of a team.
     * @param player The {@link UUID} of the player to add.
     * @return {@code true} if successful.
     */
    public boolean addMember(UUID player) {
        if (SPSSpigot.claims().getPlayerTeam(player) == null) {
            boolean out = members.add(player);
            SPSSpigot.claims().saveFile();
            return out;
        } else {
            // Player is already on a team.
            return false;
        }
    }

    /**
     * Adds a player to the team. Will not add if already a member of a team.
     * @param player The {@link OfflinePlayer} to add.
     * @return {@code true} if successful.
     */
    public boolean addMember(OfflinePlayer player) {
        return addMember(player.getUniqueId());
    }

    /**
     * Adds a player to the team. The leader may not leave unless they are the last member.
     * @param player The {@link UUID} of the player to remove.
     * @return {@code true} if successful.
     */
    public boolean removeMember(UUID player) {
        if (player.equals(leader)) {
            if (members.size() == 1) {
                // Delete the team
                SPSSpigot.claims().saveFile();
                return true;
            } else {
                return false;
            }
        }
        boolean out = members.remove(player);
        SPSSpigot.claims().saveFile();
        return out;
    }

    /**
     * Removes a player from the team. The leader may not leave unless they are the last member.
     * @param player The {@link OfflinePlayer} to remove.
     * @return {@code true} if successful.
     */
    public boolean removeMember(OfflinePlayer player) {
        return removeMember(player.getUniqueId());
    }

    /**
     * Gets the leader.
     * @return The {@link UUID} of the leader.
     */
    public UUID getLeader() {
        return leader;
    }

    /**
     * Changes the {@link Team}'s leader to another member.
     * @param newLeader The {@link UUID} of the new leader. The new leader must be a player.
     * @return {@code true} if successfully changed the leader.
     */
    public boolean changeLeader(UUID newLeader) {
        if (isMember(newLeader)) {
            leader = newLeader;
            SPSSpigot.claims().saveFile();
            return true;
        } else {
            return false;
        }
    }

    // Serialization
    private static final String NAMEKEY = "name";
    private static final String MEMBERSKEY = "members";
    private static final String LEADERKEY = "leader";

    public Team(Map<String, Object> map) {
        // Get name
        this.name = (String) map.get(NAMEKEY);
        // Get members
        List<String> memberStrs = (List<String>) map.get(MEMBERSKEY);
        this.members = new HashSet<>();
        for (String uuidStr : memberStrs)
            this.members.add(UUID.fromString(uuidStr));
        // Get leader
        this.leader = UUID.fromString((String) map.get(LEADERKEY));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        // Add name
        map.put(NAMEKEY, this.name);
        // Add members
        List<String> memberStrs = new ArrayList<>();
        for (UUID uuid : this.members)
            memberStrs.add(uuid.toString());
        map.put(MEMBERSKEY, memberStrs);
        // Add leader
        map.put(LEADERKEY, this.leader.toString());
        return map;
    }


}
