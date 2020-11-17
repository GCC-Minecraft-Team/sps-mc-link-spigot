package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.mongodb.DBObject;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WorldGroup {

    private final UUID id;
    private final String name;
    private final Set<World> worlds;
    private final Set<World> claimable;
    private final Set<Team> teams;
    private final Map<UUID, Set<Chunk>> claims;

    private final Map<UUID, Team> joinRequests;
    private final Set<UUID> overrides;

    /**
     * This is the simple constructor, generally for new instances not loaded from file.
     * @param name The name of this {@link WorldGroup}.
     */
    public WorldGroup(String name) {
        id = UUID.randomUUID();
        this.name = name;
        worlds = new HashSet<>();
        claimable = new HashSet<>();
        teams = new HashSet<>();
        claims = new HashMap<>();

        joinRequests = new HashMap<>();
        overrides = new HashSet<>();

        saveCurrentClaims();
    }

    /**
     * This constructor is used to initialize the class using a {@link WorldGroupSerializable}.
     * @param wg The {@link WorldGroupSerializable}.
     */
    public WorldGroup(WorldGroupSerializable wg) {
        this.id = wg.getID();
        this.name = wg.getName();

        this.worlds = new HashSet<>();
        for (String w : wg.getWorlds()) {
            this.worlds.add(Bukkit.getWorld(UUID.fromString(w)));
        }

        this.claimable = new HashSet<>();
        for (String w : wg.getClaimable()) {
            this.claimable.add(Bukkit.getWorld(UUID.fromString(w)));
        }

        this.teams = new HashSet<>();
        for (TeamSerializable ts : wg.getTeams()) {
            this.teams.add(new Team(ts));
        }

        this.claims = new HashMap<>();
        for (Map.Entry<String, Set<DBObject>> c : wg.getClaims().entrySet()) {
            Set<Chunk> claimChunks = new HashSet<>();
            for (DBObject ch : c.getValue()) {
                World world = SPSSpigot.server().getWorld((UUID) ch.get("world"));
                if (world != null)
                    claimChunks.add(world.getChunkAt((int) ch.get("x"), (int) ch.get("z")));
            }
            this.claims.put(UUID.fromString(c.getKey()), claimChunks);
        }

        this.joinRequests = new HashMap<>();
        this.overrides = new HashSet<>();
    }

    /**
     * Saves data from this {@link WorldGroup} to the database.
     */
    public void saveCurrentClaims() {
        DatabaseLink.saveClaims(claims, this);
    }

    /**
     * Getter for this {@link WorldGroup}'s name.
     * @return This {@link WorldGroup}'s name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Getter for this {@link WorldGroup}'s ID.
     * @return This {@link WorldGroup}'s ID.
     */
    @NotNull
    public UUID getID() { return id; }

    /**
     * Gets whether this {@link WorldGroup} contains the given {@link World}.
     * @param world The {@link World} to check for.
     * @return {@code true} if this {@link WorldGroup} contains the {@link World}.
     */
    public boolean hasWorld(@NotNull World world) {
        return worlds.contains(world);
    }

    /**
     * Gets all {@link World}s this {@link WorldGroup} contains.
     * @return An unmodifiable {@link Set} of {@link World}s.
     */
    @NotNull
    public Set<World> getWorlds() {
        return Collections.unmodifiableSet(worlds);
    }

    /**
     * Adds a {@link World} to this {@link WorldGroup} if it is not in another one.
     * @param world The {@link World} to add.
     * @return {@code true} if successful, {@code false} if the {@link World} is already in a {@link WorldGroup}.
     */
    public boolean addWorld(@NotNull World world) {
        if (SPSSpigot.getWorldGroup(world) == null) {
            worlds.add(world);
            DatabaseLink.addWorld(this, world);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a {@link World} from this {@link WorldGroup}.
     * @param world The {@link World} to remove.
     * @return {@code true} if successful, {@code false} if the {@link World} was not in this {@link WorldGroup}.
     */
    public boolean removeWorld(@NotNull World world) {
        if (worlds.remove(world)) {
            claimable.remove(world);
            DatabaseLink.removeWorld(this, world);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Getter for this {@link WorldGroup}'s claimable {@link World}s.
     * @return An unmodifiable {@link Set} of {@link WorldGroup}'s claimable {@link World}s.
     */
    @NotNull
    public Set<World> getClaimable() {
        return Collections.unmodifiableSet(claimable);
    }

    /**
     * Gets whether a specified {@link World} is claimable in this {@link WorldGroup}.
     * @param world The {@link World} to check.
     * @return {@code true} if this {@link WorldGroup}'s claimable list contains the {@link World}.
     */
    public boolean isClaimable(@NotNull World world) {
        return claimable.contains(world);
    }

    /**
     * Adds a {@link World} to this {@link WorldGroup} claimable if it is not in another one.
     * @param world The {@link World} to add.
     * @return {@code true} if successful, {@code false} if the {@link World} is already in a {@link WorldGroup}.
     */
    public boolean addWorldClaimable(@NotNull World world) {
        if (SPSSpigot.getWorldGroup(world) != null) {
            claimable.add(world);
            DatabaseLink.addWorldClaimable(this, world);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a {@link World} claimable from this {@link WorldGroup}.
     * @param world The {@link World} to remove.
     * @return {@code true} if successful, {@code false} if the {@link World} was not in this {@link WorldGroup}.
     */
    public boolean removeWorldClaimable(@NotNull World world) {
        if (claimable.remove(world)) {
            DatabaseLink.removeWorldClaimable(this, world);
            return true;
        } else {
            return false;
        }
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

    /**
     * Checks to see if entity is in spawn.
     * @param entity The {@link Entity} to check.
     * @return {@code true} if its {@link Location} {@link #isInSpawn(Location)}.
     */
    public boolean isEntityInSpawn(@NotNull Entity entity) {
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
     * @return {@link true} if successful; {@link false} if the name is already taken or the leader is already on a {@link Team}.
     */
    public boolean addTeam(@NotNull Team team) {
        if (getTeam(team.getName()) == null && getPlayerTeam(team.getLeader()) == null) {
            boolean out = teams.add(team);
            DatabaseLink.updateTeam(team);
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
            if (request.getValue().equals(team))
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
     * @return {@code true} if both players are on the same {@link Team}. Both being independent does not count.
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
        joinRequests.put(player, team);
        return joinRequests.get(player);
    }

    /**
     * Cancels a join request.
     * @param player The {@link UUID} of the player whose request should be canceled.
     * @return The {@link Team} the player was requesting, or {@code null} if none were requested (meaning this call did nothing).
     */
    @Nullable
    public Team cancelJoinRequest(@NotNull UUID player) {
        Team team = joinRequests.get(player);
        joinRequests.remove(player);
        return team;
    }

    /**
     * Fulfills a join request, adding the player to the {@link Team}.
     * @param player The {@link UUID} of the player whose request should be fulfilled.
     * @return The {@link Team} that the player was added to, or {@code null} if no request was found.
     * @throws IllegalStateException If the player was already on a {@link Team}.
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
     * Sets the claim override status of a given player.
     * @param player The {@link UUID} of the player.
     * @param value The boolean value of whether the player should be set to have claim override.
     */
    public void setOverride(@NotNull UUID player, boolean value) {
        if (value)
            overrides.add(player);
        else
            overrides.remove(player);
    }

    /**
     * Gets whether a player has claim override enabled.
     * @param player The {@link UUID} of the player to check.
     * @return {@code true} if the player has claim override enabled.
     */
    public boolean hasOverride(@NotNull UUID player) {
        return overrides.contains(player);
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
        // 16 + 32log_2(x+2)
        // Nathan: Increased max chunk scale
        return (int) (16 + 32 * Math.log((playTicks / (20.0 * 60.0 * 60.0)) + 2) / Math.log(2));
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

    // NOTE!!! READ ME!!! Don't remove this function or change it to use .equals() (for some reason that breaks the server a lot)
    @Nullable
    public UUID getChunkOwner(@NotNull Chunk chunk) {
        for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
            for (Chunk c : player.getValue()) {
                if (c.getX() == chunk.getX() && c.getZ() == chunk.getZ() && chunk.getWorld().equals(c.getWorld())) {
                    return player.getKey();
                }
            }
        }
        return null;
    }
    /**
     * Gets the owner of a chunk.
     * @param world The {@link World}.
     * @param x The chunk X.
     * @param z The chunk Z.
     * @return The {@link UUID} of the owner or {@code null} if unowned.
     */
    @Nullable
    public UUID getChunkOwner(@NotNull World world, int x, int z) {
        for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
            for (Chunk chunk : player.getValue()) {
                if (chunk.getWorld().equals(world) && chunk.getX() == x && chunk.getZ() == z) {
                    return player.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Gets the owner of a location.
     * @param world The {@link World}.
     * @param x The block X.
     * @param z The block Z.
     * @return The {@link UUID} of the owner or {@code null} if unowned.
     */
    @Nullable
    public UUID getOwner(@NotNull World world, int x, int z) {
        return getChunkOwner(world, Math.floorDiv(x, 16), Math.floorDiv(z, 16));
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
     * Gets all claims in this {@link WorldGroup}.
     * @return An unmodifiable {@link Map} of claims, where the {@link Chunk} {@link Set}s are also unmodifiable.
     */
    @NotNull
    public Map<UUID, Set<Chunk>> getClaims() {
        Map<UUID, Set<Chunk>> unmodifiables = new HashMap<>();
        for (Map.Entry<UUID, Set<Chunk>> entry : claims.entrySet())
            unmodifiables.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        return Collections.unmodifiableMap(unmodifiables);
    }

    /**
     * Claims a {@link Chunk} for a given player.
     * @param player The {@link UUID} of the player.
     * @param chunk The {@link Chunk} to be claimed.
     * @return {@code true} if successful. This means the {@link Chunk} is not already claimed, the player is not exceeding their claim limit, and the {@link World} is claimable by this {@link WorldGroup}.
     */
    public boolean claimChunk(@NotNull UUID player, @NotNull Chunk chunk) {
        if (claimable == null || claimable.isEmpty() || !claimable.contains(chunk.getWorld())) {
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
            if (claimable == null || claimable.isEmpty() || !claimable.contains(chunk.getWorld())) {
                // Ensure the world is claimable in this
            } else if (getChunkOwner(chunk) != null) {
                // Ensure the chunk isn't claimed
            } else if (getChunkCount(player) >= getMaxChunks(player)) {
                // Ensure this won't put the player over their claim limit
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
    public boolean unclaimChunk(@NotNull Chunk chunk, @NotNull UUID owner) {
        if (claims.get(owner) != null) {
            claims.get(owner).removeIf(c -> chunk.getX() == c.getX() && chunk.getZ() == c.getZ() && chunk.getWorld() == c.getWorld());
        }
        saveCurrentClaims();
        return true;
    }

    /**
     * Unclaims multiple {@link Chunk}s.
     * @param chunks The {@link Chunk}s to be unclaimed.
     * @return A {@link Set} of the {@link Chunk}s that were successfully unclaimed.
     */
    @NotNull
    public Set<Chunk> unclaimChunkSet(@NotNull Set<Chunk> chunks, @NotNull UUID owner) {
        Set<Chunk> successes = new HashSet<>();
        for (Chunk chunk : chunks) {
            if (claims.get(owner) != null) {
                claims.get(owner).removeIf(c -> chunk.getX() == c.getX() && chunk.getZ() == c.getZ() && chunk.getWorld() == c.getWorld());
            }
        }
        saveCurrentClaims();
        return successes;
    }

    /**
     * Checks whether a player is allowed to modify a chunk. If player is null, then only chunks that are unclaimed are allowed. This is to allow events that are related to unclaimed blocks.
     * @param player The {@link UUID} of the player to check, or {@code null} if relating to behavior by unclaimed blocks.
     * @param chunk The {@link Chunk} to check claims on.
     * @param isPlayer {@code true} if an actual player is doing the action, or {@code false} if blocks/entities owned by the player are doing the action.
     * @return Whether the player is allowed to modify the {@link Chunk}.
     */
    public boolean canModifyChunk(@Nullable UUID player, @NotNull Chunk chunk, boolean isPlayer) {
        UUID owner = getChunkOwner(chunk);
        if (player == null) {
            // This means the check is probably on a non-player, unclaimed block
            return owner == null;
        } else {
            // We're just checking a player's permission in a chunk.
            return owner == null || owner.equals(player) || isOnSameTeam(player, owner) || (isPlayer && hasOverride(player));
        }
    }
}
