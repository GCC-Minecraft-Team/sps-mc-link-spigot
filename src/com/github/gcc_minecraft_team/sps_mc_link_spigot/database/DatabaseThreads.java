package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseThreads {

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    private static MongoCollection<Document> userCol;
    private static MongoCollection<WorldGroup> wgCol;

    public DatabaseThreads(MongoClient mongoClient, MongoDatabase mongoDatabase, MongoCollection<Document> userCol, MongoCollection<WorldGroup> wgCol) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.userCol = userCol;
        this.wgCol = wgCol;
    }

    /**
     * Adds a {@link WorldGroup} to the database on thread.
     */
    class AddWorldGroup implements Runnable {

        private final WorldGroup worldGroup;
        AddWorldGroup(WorldGroup worldGroup) {
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
            wgCol.deleteOne(new Document("worldgroup_id", worldGroup.getID()));
        }
    }


}

