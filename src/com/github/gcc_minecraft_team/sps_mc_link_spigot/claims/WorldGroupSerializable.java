package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class WorldGroupSerializable {
    private final UUID worldgroupId;
    private final String name;
    private final Set<String> worlds;
    private final Set<String> claimable;
    private final Set<TeamSerializable> teams;
    private final Map<String, Set<DBObject>> claims;
    private final Map<String, DBObject> spawnLocations;
    // Don't bother serializing join requests as they shouldn't leave memory
    //@BsonProperty(value = "joinRequests")
    //private Map<String, Team> joinRequests;

    // MongoDB POJO constructor
    @BsonCreator
    public WorldGroupSerializable(
            @BsonProperty("WGID") final UUID WGID,
            @BsonProperty("name") final String name,
            @BsonProperty("worlds") final Set<String> worlds,
            @BsonProperty("claimable") final Set<String> claimable,
            @BsonProperty("teams") final Set<TeamSerializable> teams,
            @BsonProperty("claims") final Map<String, Set<DBObject>> claims,
            @BsonProperty("spawnLocations") final Map<String, DBObject> spawnLocations
    ) {
        this.worldgroupId = WGID;
        this.name = name;
        this.worlds = worlds;
        this.claimable = claimable;
        this.teams = teams;
        this.claims = claims;
        this.spawnLocations = spawnLocations;
    }

    // Worldgroup constructor
    public WorldGroupSerializable(WorldGroup wg) {
        this.worldgroupId = wg.getID();
        this.name = wg.getName();

        this.worlds = new HashSet<>();
        for (World w : wg.getWorlds()) {
            this.worlds.add(w.getUID().toString());
        }

        this.claimable = new HashSet<>();
        for (World w : wg.getClaimable()) {
            this.claimable.add(w.getUID().toString());
        }

        this.teams = new HashSet<>();
        for (Team t : wg.getTeams()) {
            this.teams.add(new TeamSerializable(t));
        }

        this.claims = new HashMap<>();
        for (Map.Entry<UUID, Set<Chunk>> c : wg.getClaims().entrySet()) {
            Set<DBObject> claimChunks = new HashSet<>();
            for (Chunk ch : c.getValue()) {
                DBObject dbChunk = new BasicDBObject();
                dbChunk.put("x", ch.getX());
                dbChunk.put("z", ch.getZ());
                dbChunk.put("world", ch.getWorld().getUID());
                claimChunks.add(dbChunk);
            }
            this.claims.put(c.getKey().toString(), claimChunks);
        }

        this.spawnLocations = new HashMap<>();
        for (Map.Entry<UUID, Location> l : wg.getSpawnLocations().entrySet()) {
            DBObject loc = new BasicDBObject();
            loc.put("x", l.getValue().getX());
            loc.put("y", l.getValue().getY());
            loc.put("z", l.getValue().getZ());

            this.spawnLocations.put(l.getKey().toString(), loc);
        }
    }

    @BsonProperty("WGID")
    public UUID getID() {
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
    public Set<TeamSerializable> getTeams() {
        return teams;
    }

    @BsonProperty("claims")
    public Map<String, Set<DBObject>> getClaims() {
        return claims;
    }

    @BsonProperty("spawnLocations")
    public Map<String, DBObject> getSpawnLocations() { return spawnLocations; }

}
