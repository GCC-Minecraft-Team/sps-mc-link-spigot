package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.TeamSerializable;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroupSerializable;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

public class DatabaseThreads {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private MongoCollection<Document> userCol;
    private MongoCollection<WorldGroupSerializable> wgCol;

    public DatabaseThreads(MongoClient mongoClient, MongoDatabase mongoDatabase, MongoCollection<Document> userCol, MongoCollection<WorldGroupSerializable> wgCol) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.userCol = userCol;
        this.wgCol = wgCol;
    }

    /**
     * Adds a {@link WorldGroup} to the database on thread.
     */
    class AddWorldGroup implements Runnable {

        private final WorldGroupSerializable worldGroup;
        AddWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.insertOne(worldGroup);
        }
    }

    /**
     * Removes a {@link WorldGroup} from the database on thread.
     */
    class RemoveWorldGroup implements Runnable {

        private final WorldGroup worldGroup;
        RemoveWorldGroup(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.deleteOne(new Document("WGID", worldGroup.getID()));
        }
    }

    /**
     * Adds (or updates) a {@link Team} to the database on thread.
     */
    class UpdateTeam implements Runnable {

        private final TeamSerializable team;
        UpdateTeam(TeamSerializable team) {
            this.team = team;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", team.WGID), Updates.addToSet("teams", team), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Adds (or updates) a world ID to the database on thread.
     */
    class UpdateWorld implements Runnable {

        private final World world;
        private final WorldGroup wg;

        UpdateWorld(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), Updates.addToSet("worlds", world.getUID().toString()), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Removes a world ID from the database on thread.
     */
    class RemoveWorld implements Runnable {

        private final World world;
        private final WorldGroup wg;

        RemoveWorld(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$pull", world.getUID().toString()));
        }
    }

    /**
     * Updates the claims for a specified {@link WorldGroup};
     */
    class UpdateClaims implements Runnable {
        private final WorldGroupSerializable worldGroup;
        private final Map<UUID, Set<Chunk>> claims;

        UpdateClaims(Map<UUID, Set<Chunk>> claims, WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
            this.claims = claims;
        }

        public void run() {
            // loop through players who have claims
            for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
                UUID uuid = player.getKey();
                Set<Chunk> uuidClaims = player.getValue();
                Set<DBObject> dbChunkSet = new HashSet<>();

                // add all claimed chunks
                for (Chunk chunk : uuidClaims) {
                    DBObject dbChunk = new BasicDBObject();
                    dbChunk.put("x", chunk.getX());
                    dbChunk.put("z", chunk.getZ());
                    dbChunk.put("world", chunk.getWorld().getUID());
                    dbChunkSet.add(dbChunk);
                }

                Document elementToArray = new Document("claims." + uuid.toString(), dbChunkSet);
                wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$set", elementToArray), new UpdateOptions().upsert(true));
            }
        }

    }
}

