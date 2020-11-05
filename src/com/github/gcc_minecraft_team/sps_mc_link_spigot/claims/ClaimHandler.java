package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimHandler {
    private static final String CLAIMSFILE = "claimsConfig.yml";
    private static final String CFGTEAMS = "teams";
    private static final String CFGCLAIMS = "claims";
    private FileConfiguration claimsConfig;

    private Set<Team> teams;
    private Map<UUID, Set<Chunk>> claims;
    private Map<UUID, Team> joinRequests;

    public ClaimHandler() {
        teams = new HashSet<>();
        claims = new HashMap<>();
        joinRequests = new HashMap<>();

        SPSSpigot.plugin().saveResource(CLAIMSFILE, false);
        claimsConfig = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), CLAIMSFILE));
        claimsConfig.addDefault(CFGTEAMS, new ArrayList<Team>());
        claimsConfig.addDefault(CFGCLAIMS, new HashMap<String, ArrayList<HashMap<String, Integer>>>());
        loadFile();
    }

    /**
     * Saves data from this {@link ClaimHandler} to {@value CLAIMSFILE};
     */
    public void saveFile() {
        // Save teams
        claimsConfig.set(CFGTEAMS, new ArrayList<>(teams));
        // Save claims
        Map<String, List<Map<String, Integer>>> serialClaims = new HashMap<>();
        for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
            List<Map<String, Integer>> chunks = new ArrayList<>();
            for (Chunk c : player.getValue()) {
                Map<String, Integer> chunkMap = new HashMap<>();
                chunkMap.put("x", c.getX());
                chunkMap.put("z", c.getZ());
                chunks.add(chunkMap);
            }
            serialClaims.put(player.getKey().toString(), chunks);
        }
        claimsConfig.set(CFGCLAIMS, serialClaims);

        try {
            claimsConfig.save(new File(SPSSpigot.plugin().getDataFolder(), CLAIMSFILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads data for this {@link ClaimHandler} from {@value CLAIMSFILE};
     */
    public void loadFile() {
        try {
            claimsConfig.load(new File(SPSSpigot.plugin().getDataFolder(), CLAIMSFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        // Load Teams
        List<Object> teamObjList = (List<Object>) claimsConfig.getList(CFGTEAMS);
        teams = new HashSet<>();
        for (Object teamObj : teamObjList)
            teams.add((Team) teamObj);
        // Load claims
        claims = new HashMap<>();
        MemorySection claimMem = (MemorySection) claimsConfig.get(CFGCLAIMS);
        for (String player : claimMem.getKeys(false)) {
            Set<Chunk> chunks = new HashSet<>();
            for (Map<?, ?> chunkMap : claimMem.getMapList(player)) {
                chunks.add(SPSSpigot.server().getWorlds().get(0).getChunkAt((int) chunkMap.get("x"), (int) chunkMap.get("z")));
            }
            claims.put(UUID.fromString(player), chunks);
        }
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
            saveFile();
            return out;
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
        saveFile();
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
            deleteTeam(team);
            saveFile();
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
            if (players.getValue().contains(chunk)) {
                return players.getKey();
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
     * @return {@code true} if successful. This means the {@link Chunk} is not already claimed, and the player is not exceeding their claim limit.
     */
    public boolean claimChunk(@NotNull UUID player, @NotNull Chunk chunk) {
        if (getChunkOwner(chunk) != null) {
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
            saveFile();
            return out;
        }
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
            boolean out = claims.get(owner).remove(chunk);
            saveFile();
            return out;
        }
    }

    public boolean canModifyChunk(@NotNull UUID player, @NotNull Chunk chunk) {
        UUID owner = getChunkOwner(chunk);
        return owner == null || owner.equals(player) || isOnSameTeam(player, owner);
    }
}
