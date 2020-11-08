package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WorldGroupSerializable {
    private final String worldgroupId;
    private final String name;
    private final Set<String> worlds;
    private final Set<String> claimable;
    private final Set<Team> teams;
    private final Map<String, Set<Chunk>> claims;
    // Don't bother serializing join requests as they shouldn't leave memory
    //@BsonProperty(value = "joinRequests")
    //private Map<String, Team> joinRequests;

    // MongoDB POJO constructor
    @BsonCreator
    public WorldGroupSerializable(
            @BsonProperty("WGID") final String WGID,
            @BsonProperty("name") final String name,
            @BsonProperty("worlds") final Set<String> worlds,
            @BsonProperty("claimable") final Set<String> claimable,
            @BsonProperty("teams") final Set<Team> teams,
            @BsonProperty("claims") final Map<String, Set<Chunk>> claims
    ) {
        this.worldgroupId = WGID;
        this.name = name;
        this.worlds = worlds;
        this.claimable = claimable;
        this.teams = teams;
        this.claims = claims;
    }

    // Worldgroup constructor
    public WorldGroupSerializable(WorldGroup wg) {
        this.worldgroupId = wg.getID().toString();
        this.name = wg.getName();

        this.worlds = new HashSet<>();
        for (World w : wg.getWorlds()) {
            this.worlds.add(w.getUID().toString());
        }

        this.claimable = new HashSet<>();
        for (World w : wg.getWorlds()) {
            this.claimable.add(w.getUID().toString());
        }

        this.teams = wg.getTeams();
        this.claims = new HashMap<>();
        for (Map.Entry<UUID, Set<Chunk>> c : wg.getClaims().entrySet()) {
            this.claims.put(c.getKey().toString(), c.getValue());
        }
    }

    @BsonProperty("WGID")
    public String getID() {
        return worldgroupId;
    }

    @BsonProperty("name")
    public String getName() {
        return name;
    }

    @BsonProperty("worlds")
    public Set<String> getWorlds() {
        return worlds;
    }

    @BsonProperty("claimable")
    public Set<String> getClaimable() {
        return claimable;
    }

    @BsonProperty("teams")
    public Set<Team> getTeams() {
        return teams;
    }

    @BsonProperty("claims")
    public Map<String, Set<Chunk>> getClaims() {
        return claims;
    }

}
