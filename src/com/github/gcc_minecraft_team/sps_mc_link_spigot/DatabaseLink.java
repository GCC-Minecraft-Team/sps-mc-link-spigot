package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.haoshoku.nick.api.NickAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.UUID;

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
        dbConfig.addDefault(CFGURI, "");
        dbConfig.addDefault(CFGDB, "");

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
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong connecting to the MongoDB database, is " + DBFILE + " set up correctly?");
        }
    }

    /**
     * Gets whether a player is registered on the database.
     * @param uuid The {@link UUID} of the player to check.
     * @return {@code true} if the player is registered.
     */
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
            SPSSpigot.logger().log(Level.SEVERE, "Couldn't check user from database! Error: " + exception.toString());
            return false;
        }
    }

    /**
     * Gets the MC names of all SPS users registered
     * @return
     */
    public static ArrayList<String> getAllSPSNames() {
        ArrayList<String> spsNames = new ArrayList<String>();
        for(Document doc : userCol.find()) {
            spsNames.add(doc.getString("mcName"));
        }

        return spsNames;
    }

    /**
     * Gets Bukkit players for all SPS users registered
     * @return
     */
    public static ArrayList<Player> getAllSPSPlayers() {
        ArrayList<Player> spsPlayers = new ArrayList<Player>();
        for(Document doc : userCol.find()) {
            spsPlayers.add(SPSSpigot.server().getPlayer(doc.getString("mcUUID")));
        }

        return spsPlayers;
    }

    /**
     * Gets the SPS name for a Minecraft player.
     * @param uuid The {@link UUID} of the Minecraft player.
     * @return The SPS name of the player.
     */
    public static String getSPSName(UUID uuid) {
        if (isRegistered(uuid)) {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getString("mcName");
        } else {
            return "Unregistered User";
        }
    }

    /**
     * gets the Bukkit player from an SPS username
     * @param SPSName
     * @return
     */
    public static Player getSPSPlayer(String SPSName) {
        Player player = SPSSpigot.server().getPlayer(UUID.fromString(userCol.find(new Document("mcName", SPSName)).first().getString("mcUUID")));
        if (player != null) {
            return player;
        } else {
            return null;
        }
    }

    /**
     * Checks if a player is banned.
     * @param uuid The Minecraft {@link UUID} of the player to ban.
     * @return {@code true} if the player is banned.
     **/
    public static Boolean getIsBanned(UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("banned");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Bans a player using their SPS ID
     * @param SPSuser The SPS username to ban without domain (e.g. 1absmith)
     * @return {@code true} if the SPS ID was successfully banned.
    **/
    public static boolean banPlayer(String SPSuser) {
        String spsEmail = SPSuser + "@seattleschools.org";

        SPSSpigot.logger().log(Level.INFO, "Banning player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("banned", true);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // ban the player in the database
        try {
            userCol.updateOne(new Document("oAuthEmail", spsEmail), setQuery);

            // kick them from the server
            SPSSpigot.server().getPlayer(UUID.fromString(userCol.find(new Document("oAuthEmail", spsEmail)).first().getString("mcUUID"))).kickPlayer("Wooks wike uwu've bewn banned! UwU");

            return true;
        } catch (MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong banning a player!");
            return false;
        } catch (NullPointerException exception)  {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong looking up a user to player!");
            return false;
        }
    }

    /**
     * Registers a new player in the database.
     * @param uuid The Minecraft {@link UUID} of the player.
     * @param SPSid The SPS ID of the player.
     * @param name The new name of the player.
     **/
    public static void registerPlayer(String uuid, String SPSid, String name) {
        // set UUID and Name
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("mcUUID", uuid);
        updateFields.append("mcName", name);
        updateFields.append( "banned", false);

        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        UpdateOptions options = new UpdateOptions().upsert(true);

        // update in the database
        userCol.updateOne(new Document("oAuthId", SPSid), setQuery, options);
        String email = userCol.find(new Document("oAuthId", SPSid)).first().getString("oAuthEmail");

        // get player
        Player player = SPSSpigot.server().getPlayer(UUID.fromString(uuid));

        // load permissions
        SPSSpigot.perms().loadPermissions(player);

        // set nametag
        int maxLength = Math.min(name.length(), 15);

        //NickAPI.setSkin( player, player.getName() );
        //NickAPI.setUniqueId( player, player.getName() );
        NickAPI.nick( player, name.substring(0, maxLength));
        NickAPI.refreshPlayer( player );

        // send a confirmation message
        player.sendMessage(ChatColor.BOLD.toString() +
                ChatColor.GREEN.toString() + "Successfully linked account " +
                ChatColor.GOLD.toString() + email +
                ChatColor.GREEN.toString() + " to the server! Your new username is: " +
                ChatColor.GOLD.toString() + name);

        // give starting boat
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_BOAT));

        player.sendMessage("You've spawned in the lobby, please use the included " + ChatColor.BLUE +"Starting Boat" + ChatColor.WHITE + " to leave the island!");

    }

}
