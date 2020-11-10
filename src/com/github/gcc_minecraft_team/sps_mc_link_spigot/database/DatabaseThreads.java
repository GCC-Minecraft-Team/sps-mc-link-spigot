package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.TeamSerializable;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroupSerializable;
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

    //TODO: Multithread all this

    /**
     * Adds a {@link WorldGroup} to the database on thread.
     */
    class AddWorldGroup {

        private final WorldGroupSerializable worldGroup;
        AddWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.insertOne(worldGroup);
            return;
        }
    }

    /**
     * Removes a {@link WorldGroup} from the database on thread.
     */
    class RemoveWorldGroup {

        private final WorldGroup worldGroup;
        RemoveWorldGroup(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.deleteOne(new Document("WGID", worldGroup.getID()));
            return;
        }
    }

    /**
     * Adds (or updates) a {@link Team} to the database on thread.
     */
    class UpdateWorldGroup {

        private final WorldGroupSerializable worldGroup;

        UpdateWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.replaceOne(new Document("WGID", worldGroup.getID()), worldGroup);
        }
    }

    /**
     * Adds (or updates) a world ID to the database on thread.
     */
    class UpdateWorld {

        private final World world;
        private final WorldGroup wg;

        UpdateWorld(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            if (wgCol.find(new Document("WGID", wg.getID())).first().getWorlds().contains(world)) {
                wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
                wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$push", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), Updates.addToSet("worlds", world.getUID().toString()), new UpdateOptions().upsert(true));
            return;
        }
    }

    /**
     * Removes a world ID from the database on thread.
     */
    class RemoveWorld {

        private final World world;
        private final WorldGroup wg;

        RemoveWorld(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())));
            return;
        }
    }

    /**
     * Adds (or updates) a world ID to the database on thread.
     */
    class UpdateWorldClaimable {

        private final World world;
        private final WorldGroup wg;

        UpdateWorldClaimable(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            if (wgCol.find(new Document("WGID", wg.getID())).first().getWorlds().contains(world)) {
                wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
                wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$push", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), Updates.addToSet("claimable", world.getUID().toString()), new UpdateOptions().upsert(true));
            return;
        }
    }

    /**
     * Removes a world ID from the database on thread.
     */
    class RemoveWorldClaimable {

        private final World world;
        private final WorldGroup wg;

        RemoveWorldClaimable(WorldGroup wg, World world) {
            this.wg = wg;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", wg.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())));
            return;
        }
    }

    /**
     * Updates the claims for a specified {@link WorldGroup};
     */
    class UpdateClaims {
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

            return;
        }

    }
}

