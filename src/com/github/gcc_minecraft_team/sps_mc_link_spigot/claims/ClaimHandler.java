package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimHandler {

    private String name;
    private Set<World> worlds;
    private Set<World> claimable;
    private Set<Team> teams;
    private Map<UUID, Set<Chunk>> claims;
    private Map<UUID, Team> joinRequests;

    /**
     * This is the simple constructor, generally for new instances not loaded from file.
     * @param name The name of this worldGroup.
     */
    public ClaimHandler(String name) {
        this.name = name;
        worlds = new HashSet<>();
        claimable = new HashSet<>();
        teams = new HashSet<>();
        claims = new HashMap<>();
        joinRequests = new HashMap<>();
        saveCurrentClaims();
    }



    /**
     * Saves data from this {@link ClaimHandler} to com.github.gcc_minecraft_team.sps_mc_link_spigot.database
     */
    public void saveCurrentClaims() {
        DatabaseLink.saveClaims(claims, this);
    }

    /**
     * Loads data for this {@link ClaimHandler} from the com.github.gcc_minecraft_team.sps_mc_link_spigot.database
     */
    public void loadFromDatabase() {
        teams = DatabaseLink.getTeams(this);
        claims = DatabaseLink.getClaims(this);
    }

    /**
     * Getter for this worldGroup's name.
     * @return This worldGroup's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets whether this {@link ClaimHandler}'s worldGroup contains the given {@link World}.
     * @param world The {@link World} to check for.
     * @return {@code true} if this worldGroup contains the {@link World}.
     */
    public boolean hasWorld(World world) {
        return worlds.contains(world);
    }

    /**
     * Checks to see if a {@link Location} is within spawn protection
     * @param location The {@link Location} to check.
     * @return {@code true} if the {@link Location} is within the spawn radius.
     */
    public boolean isInSpawn(@NotNull Location location) {
        double zdist = location.getZ() - location.getWorld().getSpawnLocation().getZ();
        double xdist = location.getX() - location.getWorld().getSpawnLocation().getX();
        return Math.abs(zdist) <= SPSSpigot.server().getSpawnRadius() && Math.abs(xdist) <= SPSSpigot.server().getSpawnRadius();
    }

    /***
     * Checks to see if entity is in spawn
     * @param entity
     * @return
     */
    public boolean isEntityInSpawn(Entity entity) {
        return isInSpawn(entity.getLocation());
    }


    /**
     * Gets all known {@link Team}s.
     * @return An unmodifiable {@link Set} of {@link Team}s.
     */
    @NotNull
    public Set<Team> getTeams() {
        return Collections.unmodifiableSet(teams);
    }

    /**
     * Gets the names of all known {@link Team}s.
     * @return A {@link Set} of {@link Team} names.
     */
    @NotNull
    public Set<String> getTeamNames() {
        Set<String> names = new HashSet<>();
        for (Team t : teams)
            names.add(t.getName());
        return names;
    }

    /**
     * Gets a {@link Team} by its name.
     * @param name The name of the {@link Team} to find.
     * @return The found {@link Team}, or {@code null} if none exist by the given name.
     */
    @Nullable
    public Team getTeam(@NotNull String name) {
        for (Team team : teams)
            if (team.getName().equalsIgnoreCase(name))
                return team;
        return null;
    }

    /**
     * Gets the {@link Team} a given player is on.
     * @param player The {@link UUID} of the player to check.
     * @return The {@link Team} the player is on, or {@code null} if not on a team.
     */
    @Nullable
    public Team getPlayerTeam(@NotNull UUID player) {
        for (Team team : teams) {
            if (team.isMember(player))
                return team;
        }
        return null;
    }

    /**
     * Adds a new {@link Team}.
     * @param team The {@link Team} to add.
     * @return {@link true} if successful; {@link false} if the name is already taken or the leader is already on a team.
     */
    public boolean addTeam(@NotNull Team team) {
        if (getTeam(team.getName()) == null && getPlayerTeam(team.getLeader()) == null) {
            boolean out = teams.add(team);
            DatabaseLink.addTeam(team);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes a given {@link Team}.
     * @param team The {@link Team} to delete.
     */
    public void deleteTeam(@NotNull Team team) {
        teams.remove(team);
        // Delete all join requests for this team
        Set<UUID> remove = new HashSet<>();
        for (Map.Entry<UUID, Team> request : joinRequests.entrySet()) {
            if (request.getValue() == team)
                remove.add(request.getKey());
        }
        for (UUID request : remove)
            joinRequests.remove(request);
        DatabaseLink.removeTeam(team);
    }

    /**
     * Deletes a given {@link Team}.
     * @param team The name of the {@link Team} to delete.
     * @return {@code true} if successful; {@code false} if no {@link Team} by the given name was found.
     */
    public boolean deleteTeam(@NotNull String team) {
        Team teamObj = getTeam(team);
        if (teamObj == null) {
            // No team by this name exists.
            return false;
        } else {
            deleteTeam(teamObj);
            DatabaseLink.removeTeam(teamObj);
            return true;
        }
    }

    /**
     * Finds whether or not two players are on the same {@link Team}.
     * @param player1 The {@link UUID} of the first player to check.
     * @param player2 The {@link UUID} of the second player to check.
     * @return {@link true} if both players are on the same {@link Team}. Both being independent does not count.
     */
    public boolean isOnSameTeam(@NotNull UUID player1, @NotNull UUID player2) {
        Team team = getPlayerTeam(player1);
        return team != null && team.isMember(player2);
    }

    /**
     * Adds a new join request, replacing a previous one if it existed.
     * @param player The {@link UUID} of the player requesting to join.
     * @param team The {@link Team} to which the request is addressed.
     * @return The previously requested {@link Team} that was replaced, or {@code null} if none was previously requested.
     * @throws IllegalStateException If the player is already on a team.
     */
    @Nullable
    public Team newJoinRequest(@NotNull UUID player, @NotNull Team team) {
        if (getPlayerTeam(player) != null)
            throw new IllegalStateException("Cannot create join request for player (UUID: " + player.toString() + ") because they are already on a team.");
        // Get the previous team - will be null if none found.
        Team old = joinRequests.get(player);
        joinRequests.put(player, team);
        return old;
    }

    /**
     * Cancels a join request.
     * @param player The {@link UUID} of the player whose request should be canceled.
     * @return The {@link Team} the player was requesting, or {@code null} if none were requested (meaning this call did nothing).
     */
    @Nullable
    public Team cancelJoinRequest(@NotNull UUID player) {
        Team team = joinRequests.get(player);
        if (team != null)
            joinRequests.remove(player);
        return team;
    }

    /**
     * Fulfills a join request, adding the player to the {@link Team}.
     * @param player The {@link UUID} of the player whose request should be fulfilled.
     * @return The {@link Team} that the player was added to, or {@code null} if no request was found.
     * @throws IllegalStateException If the player was already on a team.
     */
    @Nullable
    public Team fulfillJoinRequest(@NotNull UUID player) {
        if (getPlayerTeam(player) != null)
            throw new IllegalStateException("Cannot fulfill a join request for player (UUID: " + player.toString() + ") because they are already on a team.");
        Team team = joinRequests.get(player);
        if (team != null)
            team.addMember(player);
            joinRequests.remove(player);
        return team;
    }

    /**
     * Gets the specified player's join request target.
     * @param player The {@link UUID} of the player to search for.
     * @return The {@link Team} requested, or {@code null} if no request was found.
     */
    @Nullable
    public Team getJoinRequest(@NotNull UUID player) {
        return joinRequests.get(player);
    }

    /**
     * Gets all the join request addressed to a specified {@link Team}.
     * @param team The {@link Team} to search for.
     * @return A {@link Set} of the {@link UUID}s of the players with join requests addressed to the {@link Team}.
     */
    @NotNull
    public Set<UUID> getTeamJoinRequests(@NotNull Team team) {
        Set<UUID> requests = new HashSet<>();
        for (Map.Entry<UUID, Team> req : joinRequests.entrySet()) {
            if (req.getValue() == team)
                requests.add(req.getKey());
        }
        return requests;
    }

    /**
     * Calculates the maximum number of {@link Chunk}s a player can claim, based on their play time.
     * Formula where h is the number of hours online: {@code 16 + 8log_{2}(h+2)}
     * @param player The {@link OfflinePlayer} to check.
     * @return Maximum number of {@link Chunk}s the player can claim.
     */
    public int getMaxChunks(@NotNull OfflinePlayer player) {
        int playTicks;
        try {
            // This is actually the number of ticks played, not minutes. The variable name is just a lie.
            playTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        } catch (IllegalArgumentException e) {
            playTicks = 0;
        }
        // 16 + 8log_2(x+2)
        return (int) (16 + 8 * Math.log((playTicks / (20.0 * 60.0 * 60.0)) + 2) / Math.log(2));
    }

    /**
     * Calculates the maximum number of {@link Chunk}s a player can claim, based on their play time.
     * Formula where h is the number of hours online: {@code 16 + 8log_{2}(h+2)}
     * @param player The {@link UUID} of the player.
     * @return Maximum number of {@link Chunk}s the player can claim.
     */
    public int getMaxChunks(@NotNull UUID player) {
        return getMaxChunks(SPSSpigot.server().getOfflinePlayer(player));
    }

    /**
     * Gets the number of {@link Chunk}s the player currently has claimed.
     * @param player The {@link UUID} of the player to check.
     * @return The number of {@link Chunk}s the player has claimed.
     */
    public int getChunkCount(@NotNull UUID player) {
        if (claims.containsKey(player))
            return claims.get(player).size();
        else
            return 0;
    }

    /**
     * Gets the owner of a given {@link Chunk}.
     * @param chunk The {@link Chunk} to check.
     * @return The {@link UUID} of the owner, or {@code null} if unowned.
     */
    @Nullable
    public UUID getChunkOwner(@NotNull Chunk chunk) {
        for (Map.Entry<UUID, Set<Chunk>> players : claims.entrySet()) {
            for (Chunk c : players.getValue()) {
                if (c.getX() == chunk.getX() && c.getZ() == chunk.getZ()) {
                    return players.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Gets all {@link Chunk}s claimed by a given player.
     * @param player The {@link UUID} of the player to check.
     * @return An unmodifiable {@link Set} of {@link Chunk}s claimed by the player. Will be empty if none are claimed.
     */
    @NotNull
    public Set<Chunk> getPlayerClaims(@NotNull UUID player) {
        if (claims.containsKey(player))
            return Collections.unmodifiableSet(claims.get(player));
        else
            return Collections.unmodifiableSet(new HashSet<>());
    }

    /**
     * Claims a {@link Chunk} for a given player.
     * @param player The {@link UUID} of the player.
     * @param chunk The {@link Chunk} to be claimed.
     * @return {@code true} if successful. This means the {@link Chunk} is not already claimed, the player is not exceeding their claim limit, and the {@link World} is claimable by this {@link ClaimHandler}.
     */
    public boolean claimChunk(@NotNull UUID player, @NotNull Chunk chunk) {
        if (!claimable.contains(chunk.getWorld())) {
            // Ensure the world is claimable in this
            return false;
        } else if (getChunkOwner(chunk) != null) {
            // Ensure the chunk isn't claimed
            return false;
        } else if (getChunkCount(player) >= getMaxChunks(player)) {
            // Ensure this won't put the player over their claim limit
            return false;
        } else {
            if (!claims.containsKey(player)) {
                claims.put(player, new HashSet<>());
            }
            boolean out = claims.get(player).add(chunk);
            saveCurrentClaims();
            return out;
        }
    }

    /**
     * Claims a list of {@link Chunk}s for a given player
     * @param player The {@link UUID} of the player.
     * @param chunks The {@link Set} of {@link Chunk}s to be claimed.
     * @return A {@link Set} of successfully claimed {@link Chunk}s. This means the {@link Chunk}s are not already claimed, the player is not exceeding their claim limit, and the {@link World}s are claimable.
     */
    @NotNull
    public Set<Chunk> claimChunkSet(@NotNull UUID player, @NotNull Set<Chunk> chunks) {
        Set<Chunk> successes = new HashSet<>();
        for (Chunk chunk : chunks) {
            if (!claimable.contains(chunk.getWorld())) {
                // Ensure the world is claimable in this
                continue;
            } else if (getChunkOwner(chunk) != null) {
                // Ensure the chunk isn't claimed
                continue;
            } else if (getChunkCount(player) >= getMaxChunks(player)) {
                // Ensure this won't put the player over their claim limit
                continue;
            } else {
                if (!claims.containsKey(player)) {
                    claims.put(player, new HashSet<>());
                }
                if (claims.get(player).add(chunk))
                    successes.add(chunk);
            }
        }
        saveCurrentClaims();
        return successes;
    }

    /**
     * Unclaims a {@link Chunk}.
     * @param chunk The {@link Chunk} to be unclaimed.
     * @return {@code true} if successful; {@code false} if already not claimed.
     */
    public boolean unclaimChunk(@NotNull Chunk chunk) {
        UUID owner = getChunkOwner(chunk);
        if (owner == null) {
            return false;
        } else {
            claims.get(owner).forEach((x) -> {
                if (x.getX() == chunk.getX() && x.getZ() == chunk.getZ()) {
                    claims.get(owner).remove(x);
                }
            });
            saveCurrentClaims();
            return true;
        }
    }

    /**
     * Unclaims multiple {@link Chunk}s.
     * @param chunks The {@link Chunk}s to be unclaimed.
     * @return A {@link Set} of the {@link Chunk}s that were successfully unclaimed.
     */
    @NotNull
    public Set<Chunk> unclaimChunkSet(@NotNull Set<Chunk> chunks) {
        Set<Chunk> successes = new HashSet<>();
        for (Chunk chunk : chunks) {
            UUID owner = getChunkOwner(chunk);
            if (owner != null && claims.get(owner).remove(chunk)) {
                successes.add(chunk);
            }
        }
        saveCurrentClaims();
        return successes;
    }

    /**
     * Checks whether a player is allowed to modify a chunk. If player is null, then only chunks that are unclaimed are allowed. This is to allow events that are related to unclaimed blocks.
     * @param player The {@link UUID} of the player to check, or {@code null} if relating to unclaimed blocks.
     * @param chunk The {@link Chunk} to check claims on.
     * @return Whether the player is allowed to modify the {@link Chunk}.
     */
    public boolean canModifyChunk(@Nullable UUID player, @NotNull Chunk chunk) {
        UUID owner = getChunkOwner(chunk);
        if (player == null) {
            // This means the check is probably on a non-player, unclaimed block
            return owner == null;
        } else {
            // We're just checking a player's permission in a chunk.
            return owner == null || owner.equals(player) || isOnSameTeam(player, owner);
        }
    }

    /**
     * updates the claim map in the scoreboard
     * @param player
     */
    public void updateClaimMap(Player player) {
        Chunk playerChunk = player.getLocation().getChunk();
        FastBoard board = SPSSpigot.plugin().boards.get(player.getUniqueId());
        if (board != null && !board.isDeleted()) {
            // map
            String[] rows = new String[7];

            for (int z = -3; z <= 3; z++) {
                StringBuilder bRow = new StringBuilder();
                for (int x = -3; x <= 3; x++) {
                    // get the surrounding chunks

                    Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                    UUID chunkOwner = getChunkOwner(chunk);
                    if (x == 0 && z == 0) {
                        bRow.append(ChatColor.BLUE).append("Ⓟ");
                    } else {
                        if (chunkOwner == null) {
                            // Unowned / in spawn
                            bRow.append(ChatColor.GRAY).append("▒");
                        } else if (chunkOwner.equals(player.getUniqueId())) {
                            // Player owns chunk
                            bRow.append(ChatColor.GREEN).append("█");
                        } else if (isOnSameTeam(player.getUniqueId(), chunkOwner)) {
                            // Teammate owns chunk
                            bRow.append(ChatColor.AQUA).append("▒");
                        } else {
                            // Other player owns chunk
                            bRow.append(ChatColor.RED).append("▒");
                        }
                    }
                }
                rows[z + 3] = bRow.toString();
            }

            // update the board
            board.updateLines(
                    rows[0],
                    rows[1],
                    rows[2],
                    rows[3],
                    rows[4],
                    rows[5],
                    rows[6]
            );
        }
    }
}
