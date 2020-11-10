package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroupSerializable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DatabaseThreads {

    public static final int PRIORITY = 2;

    /**
     * Adds a {@link WorldGroup} to the database on thread.
     */
    public static class AddWorldGroup implements Runnable {

        private final WorldGroupSerializable worldGroup;

        public AddWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        @Override
        public void run() {
            DatabaseLink.wgCol.insertOne(worldGroup);
        }
    }

    /**
     * Removes a {@link WorldGroup} from the database on thread.
     */
    public static class RemoveWorldGroup implements Runnable {

        private final WorldGroup worldGroup;

        public RemoveWorldGroup(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
        }

        @Override
        public void run() {
            DatabaseLink.wgCol.deleteOne(new Document("WGID", worldGroup.getID()));
        }
    }

    /**
     * Adds (or updates) a {@link WorldGroup} to the database on thread.
     */
    public static class UpdateWorldGroup implements Runnable {

        private final WorldGroupSerializable worldGroup;

        public UpdateWorldGroup(WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
        }

        @Override
        public void run() {
            DatabaseLink.wgCol.replaceOne(new Document("WGID", worldGroup.getID()), worldGroup);
        }
    }

    /**
     * Adds a {@link World} to a {@link WorldGroup} on the database on thread.
     */
    public static class UpdateWorld implements Runnable {

        private final World world;
        private final WorldGroup worldGroup;

        public UpdateWorld(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        @Override
        public void run() {
            if (DatabaseLink.wgCol.find(new Document("WGID", worldGroup.getID())).first().getWorlds().contains(world)) {
                DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
                DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$push", new Document("worlds", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), Updates.addToSet("worlds", world.getUID().toString()), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Removes a {@link World} from a {@link WorldGroup} on the database on thread.
     */
    public static class RemoveWorld implements Runnable {

        private final World world;
        private final WorldGroup worldGroup;

        public RemoveWorld(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        @Override
        public void run() {
            DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("worlds", world.getUID().toString())));
        }
    }

    /**
     * Adds a {@link World} to the claimable list of a {@link WorldGroup} on the database on thread.
     */
    public static class UpdateWorldClaimable implements Runnable {

        private final World world;
        private final WorldGroup worldGroup;

        public UpdateWorldClaimable(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        @Override
        public void run() {
            if (DatabaseLink.wgCol.find(new Document("WGID", worldGroup.getID())).first().getWorlds().contains(world)) {
                DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
                DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$push", new Document("claimable", world.getUID().toString())), new UpdateOptions().upsert(true));
            }
            DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), Updates.addToSet("claimable", world.getUID().toString()), new UpdateOptions().upsert(true));
        }
    }

    /**
     * Removes a {@link World} from the claimable list of a {@link WorldGroup} on the database on thread.
     */
    public static class RemoveWorldClaimable implements Runnable {

        private final World world;
        private final WorldGroup worldGroup;

        public RemoveWorldClaimable(WorldGroup worldGroup, World world) {
            this.worldGroup = worldGroup;
            this.world = world;
        }

        @Override
        public void run() {
            DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$pull", new Document("claimable", world.getUID().toString())));
        }
    }

    /**
     * Updates the claims for a specified {@link WorldGroup};
     */
    public static class UpdateClaims implements Runnable {
        private final WorldGroupSerializable worldGroup;
        private final Map<UUID, Set<Chunk>> claims;

        public UpdateClaims(Map<UUID, Set<Chunk>> claims, WorldGroupSerializable worldGroup) {
            this.worldGroup = worldGroup;
            this.claims = claims;
        }

        @Override
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
                DatabaseLink.wgCol.updateOne(Filters.eq("WGID", worldGroup.getID()), new Document("$set", elementToArray), new UpdateOptions().upsert(true));
            }
        }
    }
}

