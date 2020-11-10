package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
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
    public class AddWorldGroup {

        private final WorldGroupSerializable worldGroup;

        public AddWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.insertOne(worldGroup);
        }
    }

    /**
     * Removes a {@link WorldGroup} from the database on thread.
     */
    public class RemoveWorldGroup {

        private final WorldGroup worldGroup;

        public RemoveWorldGroup(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.deleteOne(new Document("WGID", worldGroup.getID()));
        }
    }

    /**
     * Adds (or updates) a {@link WorldGroup} to the database on thread.
     */
    public class UpdateWorldGroup {

        private final WorldGroupSerializable worldGroup;

        public UpdateWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        public void run() {
            wgCol.replaceOne(new Document("WGID", worldGroup.getID()), worldGroup);
        }
    }

    /**
     * Adds a {@link World} to a {@link WorldGroup} on the database on thread.
     */
    public class UpdateWorld {

        private final World world;
        private final WorldGroup worldGroup;

        public UpdateWorld(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        public void run() {
            if (wgCol.find(new Document("WGID", worldGroup.getID())).first().getWorlds().contains(world)) {
                wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
                wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$push", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), Updates.addToSet("worlds", world.getUID().toString()), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Removes a {@link World} from a {@link WorldGroup} on the database on thread.
     */
    public class RemoveWorld {

        private final World world;
        private final WorldGroup worldGroup;

        public RemoveWorld(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())));
        }
    }

    /**
     * Adds a {@link World} to the claimable list of a {@link WorldGroup} on the database on thread.
     */
    public class UpdateWorldClaimable {

        private final World world;
        private final WorldGroup worldGroup;

        public UpdateWorldClaimable(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        public void run() {
            if (wgCol.find(new Document("WGID", worldGroup.getID())).first().getWorlds().contains(world)) {
                wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
                wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$push", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), Updates.addToSet("claimable", world.getUID().toString()), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Removes a {@link World} from the claimable list of a {@link WorldGroup} on the database on thread.
     */
    public class RemoveWorldClaimable {

        private final World world;
        private final WorldGroup worldGroup;

        public RemoveWorldClaimable(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        public void run() {
            wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())));
        }
    }

    /**
     * Updates the claims for a specified {@link WorldGroup};
     */
    public class UpdateClaims {
        private final WorldGroupSerializable worldGroup;
        private final Map<UUID, Set<Chunk>> claims;

        public UpdateClaims(Map<UUID, Set<Chunk>> claims, WorldGroupSerializable worldGroup) {
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

