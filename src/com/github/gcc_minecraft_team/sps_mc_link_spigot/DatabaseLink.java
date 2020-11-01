package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import java.util.UUID;

// mongodb
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// yaml
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DatabaseLink {

    public static final String DBFILE = "databaseConfig.yml";
    public static final String CFGDB = "dbDB";
    public static final String CFGURI = "dbURI";

    // mongo variables
    private static FileConfiguration dbConfig;
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    private static MongoCollection<Document> userCol;

    /**
     * Creates a connection to the MongoDB database
     */
    public static void SetupDatabase() {
        // create config if it doesn't exist
        SPSSpigot.plugin().saveResource(DBFILE, false);
        dbConfig = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), DBFILE));
        dbConfig.addDefault(CFGURI, new String());
        dbConfig.addDefault(CFGDB, new String());

        // load config
        try {
            dbConfig.load(new File(SPSSpigot.plugin().getDataFolder(), DBFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // set up info for database
        String connectionString = (String) dbConfig.get(CFGURI);
        String dbName = (String) dbConfig.get(CFGDB);

        // set database and connect
        try {
            mongoClient = MongoClients.create(connectionString);
            mongoDatabase = mongoClient.getDatabase(dbName);
            userCol = mongoDatabase.getCollection("users");
        } catch(MongoException exception) {
            System.out.println("Something went wrong connecting to the MongoDB database, is " + DBFILE + " set up correctly?");
        }
    }

    public static boolean isRegistered(UUID uuid) {

        try {
            // check if player is registered
            if (userCol.countDocuments(new Document("mcUUID", uuid.toString())) == 1) {
                // player has registered
                return true;
            } else {
                // player hasn't registered
                return false;
            }
        } catch(MongoException exception) {
            System.out.println("Couldn't check user from database! Error: " + exception.toString());
            return false;
        }
    }

    public static String getSPSName(UUID uuid) {

        if (isRegistered(uuid)) {
            Document user = userCol.find(new Document("mcUUID", uuid)).first();
            return user.getString("mcName");
        } else {
            return "Unregistered User";
        }
    }

    public static void registerPlayer(String uuid, String SPSid, String name) {
        // set UUID and Name
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("mcUUID", uuid);
        updateFields.append("mcName", name);

        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // update in the database
        userCol.updateOne(new Document("oAuthId", SPSid), setQuery);
    }

}
