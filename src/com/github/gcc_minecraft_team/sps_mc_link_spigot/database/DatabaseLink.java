package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CompassThread;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.*;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.haoshoku.nick.api.NickAPI;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DatabaseLink {

    public static final String DBFILE = "databaseConfig.yml";
    public static final String CFGDB = "dbDB";
    public static final String CFGURI = "dbURI";

    // mongo variables
    private static FileConfiguration dbConfig;
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    public static MongoCollection<Document> userCol;
    public static MongoCollection<WorldGroupSerializable> wgCol;

    /**
     * Creates a connection to the MongoDB database.
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
        ConnectionString connectionString = new ConnectionString((String) dbConfig.get(CFGURI));
        String dbName = (String) dbConfig.get(CFGDB);

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);


        // set client settings
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .codecRegistry(codecRegistry).build();

        // set database and connect
        try {
            mongoClient = MongoClients.create(clientSettings);
            mongoDatabase = mongoClient.getDatabase(dbName);
            userCol = mongoDatabase.getCollection("users");
            wgCol = mongoDatabase.getCollection("worldgroups", WorldGroupSerializable.class);
        } catch(MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong connecting to the MongoDB database, is " + DBFILE + " set up correctly?");
        }
    }

    /**
     * Gets all the {@link WorldGroup}s. Should generally be only called on startup.
     * @return A {@link Set} of all {@link WorldGroup}s.
     */
    @NotNull
    public static Set<WorldGroup> getWorldGroups() {
        MongoCursor<WorldGroupSerializable> cur = wgCol.find().iterator();
        Set<WorldGroup> output = new HashSet<>();
        while(cur.hasNext()) {
            WorldGroupSerializable wgs = cur.next();
            WorldGroup wg = new WorldGroup(wgs);
            output.add(wg);
        }
        return output;
    }

    /**
     * Adds a {@link WorldGroup} to the database. This should not be called to update the {@link WorldGroup}.
     * @param worldGroup The {@link WorldGroup} to add.
     */
    public static void addWorldGroup(@NotNull WorldGroup worldGroup) {
        Thread thread = new Thread(new DatabaseThreads.AddWorldGroup(new WorldGroupSerializable(worldGroup)));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Remove a {@link WorldGroup} from the database.
     * @param worldGroup The {@link WorldGroup} to remove.
     */
    public static void removeWorldGroup(@NotNull WorldGroup worldGroup) {
        Thread thread = new Thread(new DatabaseThreads.RemoveWorldGroup(worldGroup));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Adds a new {@link World} to the database.
     * @param worldGroup The {@link WorldGroup} to add the {@link World} to.
     * @param world The {@link World} to add.
     */
    public static void addWorld(@NotNull WorldGroup worldGroup, @NotNull World world) {
        Thread thread = new Thread(new DatabaseThreads.UpdateWorld(worldGroup, world));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Removes a {@link World} from the database.
     * @param worldGroup The {@link WorldGroup} to remove the {@link World} from.
     * @param world The {@link World} to remove.
     */
    public static void removeWorld(@NotNull WorldGroup worldGroup, @NotNull World world) {
        Thread thread = new Thread(new DatabaseThreads.RemoveWorld(worldGroup, world));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Adds a new {@link World} to the claimable list on the database.
     * @param worldGroup The {@link WorldGroup} to add the {@link World} to.
     * @param world The {@link World} to add.
     */
    public static void addWorldClaimable(@NotNull WorldGroup worldGroup, @NotNull World world) {
        Thread thread = new Thread(new DatabaseThreads.UpdateWorldClaimable(worldGroup, world));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Removes a {@link World} from the claimable list on the database.
     * @param worldGroup The {@link WorldGroup} to remove the {@link World} from.
     * @param world The {@link World} to remove.
     */
    public static void removeWorldClaimable(@NotNull WorldGroup worldGroup, @NotNull World world) {
        Thread thread = new Thread(new DatabaseThreads.RemoveWorldClaimable(worldGroup, world));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Gets all {@link Team}s in the database.
     * @param worldGroup The {@link WorldGroup} from which to find the {@link Team}.
     * @return A {@link Set} of {@link Team}s found.
     */
    @NotNull
    public static Set<Team> getTeams(@NotNull WorldGroup worldGroup) {
        Set<TeamSerializable> cur = wgCol.find(Filters.eq("WGID", worldGroup.getID())).first().getTeams();
        Set<Team> output = new HashSet<>();
        for (TeamSerializable ts : cur) {
            Team t = new Team(ts);
            output.add(t);
        }
        return output;
    }

    /**
     * Adds (if does not exist) or updates (if does exist) a {@link Team} in the database.
     * @param team The {@link Team} to add/update.
     */
    public static void updateTeam(@NotNull Team team) {
        Thread thread = new Thread(new DatabaseThreads.UpdateWorldGroup(new WorldGroupSerializable(team.getWorldGroup())));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Removes a {@link Team} from the database.
     * @param team The {@link Team} to remove.
     */
    public static void removeTeam(@NotNull Team team) {
        Thread thread = new Thread(new DatabaseThreads.UpdateWorldGroup(new WorldGroupSerializable(team.getWorldGroup())));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Gets claims from the database.
     * @param worldGroup The {@link WorldGroup} from which to get claims.
     * @return The {@link WorldGroup}'s claim {@link Map}.
     */
    @NotNull
    public static Map<UUID, Set<Chunk>> getClaims(@NotNull WorldGroup worldGroup) {
        return new WorldGroup(wgCol.find(Filters.eq("WGID", worldGroup.getID())).first()).getClaims();
    }

    /**
     * Saves claims to the database.
     * @param claims The claim {@link Map} to save.
     * @param worldGroup The {@link WorldGroup} to which to save the claims.
     */
    public static void saveClaims(@NotNull Map<UUID, Set<Chunk>> claims, @NotNull WorldGroup worldGroup) {
        Thread thread = new Thread(new DatabaseThreads.UpdateClaims(claims, new WorldGroupSerializable(worldGroup)));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Updates a whole {@link WorldGroup} in the database.
     * @param wg The {@link WorldGroup} to update.
     */
    public static void updateWorldGroup(@NotNull WorldGroup wg) {
        Thread thread = new Thread(new DatabaseThreads.UpdateWorldGroup(new WorldGroupSerializable(wg)));
        thread.setPriority(DatabaseThreads.PRIORITY);
        thread.start();
    }

    /**
     * Gets whether a player is registered on the database.
     * @param uuid The {@link UUID} of the player to check.
     * @return {@code true} if the player is registered.
     */
    public static boolean isRegistered(@NotNull UUID uuid) {
        try {
            // check if player is registered
            return userCol.countDocuments(new Document("mcUUID", uuid.toString())) == 1;
        } catch(MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Couldn't check user from database! Error: " + exception.toString());
            return false;
        }
    }

    /**
     * Gets the Minecraft names of all SPS users registered.
     * @return A {@link List} of all the names.
     */
    @NotNull
    public static List<String> getAllSPSNames() {
        List<String> spsNames = new ArrayList<>();
        for(Document doc : userCol.find()) {
            spsNames.add(doc.getString("mcName"));
        }
        return spsNames;
    }

    /**
     * Gets the {@link UUID}s of all SPS users registered.
     * @return A {@link List} of all players' {@link UUID}s.
     */
    @NotNull
    public static List<UUID> getAllSPSPlayers() {
        List<UUID> spsPlayers = new ArrayList<>();
        for (Document doc : userCol.find()) {
            spsPlayers.add(UUID.fromString(doc.getString("mcUUID")));
        }
        return spsPlayers;
    }

    /**
     * Gets the SPS Names of all players currently on the server.
     * @return A {@link List} of all online players' names.
     */
    @NotNull
    public static List<String> getPlayerNames(){
        List<String> names = new ArrayList<>();
        for (Player p : SPSSpigot.server().getOnlinePlayers()){
            String name = getSPSName(p.getUniqueId());
            if (!name.equals("Unregistered User"))
                names.add(name);
        }
        return names;
    }

    /**
     * Gets the SPS name for a Minecraft player.
     * @param uuid The {@link UUID} of the Minecraft player.
     * @return The SPS name of the player.
     */
    @NotNull
    public static String getSPSName(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getString("mcName");
        } else {
            return "Unregistered User";
        }
    }

    /**
     * Gets the school a player goes to.
     * @param uuid The {@link UUID} of the player to check.
     * @return The three-character tag of the school, or an empty {@link String} if not found.
     */
    public static String getSchoolTag(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            String fullName = userCol.find(new Document("mcUUID", uuid.toString())).first().getString("oAuthName");
            if (fullName != null) {
                return fullName.split(" ")[4].trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Gets the grade level of a player.
     * @param uuid The {@link UUID} of the player to check.
     * @return The one or two-character grade level, or an empty {@link String} if not found.
     */
    public static String getGradeTag(@NotNull UUID uuid) {
        if (isRegistered(uuid)) {
            String fullName = userCol.find(new Document("mcUUID", uuid.toString())).first().getString("oAuthName");
            if (fullName != null) {
                return fullName.split(" ")[5].trim();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * Gets the {@link UUID} of the Minecraft player from their SPS username.
     * @param SPSName The SPS username to check.
     * @return The {@link UUID} of the Minecraft player if they are linked, otherwise {@code null}.
     */
    @Nullable
    public static UUID getSPSUUID(@NotNull String SPSName) {
        Document result = userCol.find(new Document("mcName", SPSName)).first();
        if (result != null)
            return UUID.fromString(result.getString("mcUUID"));
        else
            return null;
    }

    /**
     * Gets the Minecraft {@link Player} from an SPS username.
     * @param SPSName The SPS username to check.
     * @return The {@link Player} if they are linked and online, otherwise {@code null}.
     */
    @Nullable
    public static OfflinePlayer getSPSPlayer(@NotNull String SPSName) {
        UUID uuid = getSPSUUID(SPSName);
        if (uuid != null)
            return SPSSpigot.server().getOfflinePlayer(uuid);
        else
            return null;
    }

    /**
     * Checks if a player is banned.
     * @param uuid The Minecraft {@link UUID} of the player to check.
     * @return {@code true} if the player is banned.
     */
    public static boolean isBanned(@NotNull UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("banned");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Bans a player using their SPS ID.
     * @param SPSUser The SPS username to ban without domain (e.g. 1absmith).
     * @return {@code true} if the SPS ID was successfully banned.
     */
    public static boolean banPlayer(@NotNull String SPSUser) {
        String spsEmail = SPSUser + "@seattleschools.org";

        SPSSpigot.logger().log(Level.INFO, "Banning player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("banned", true);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // Ban the player in the database
        try {
            userCol.updateOne(new Document("oAuthEmail", spsEmail), setQuery);

            // kick them from the server
            OfflinePlayer oplayer = SPSSpigot.server().getOfflinePlayer(UUID.fromString(userCol.find(new Document("oAuthEmail", spsEmail)).first().getString("mcUUID")));
            if (oplayer.isOnline()) {
                oplayer.getPlayer().kickPlayer("Wooks wike uwu've bewn banned! UwU");
            }

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
     * Checks if a player is muted.
     * @param uuid The Minecraft {@link UUID} of the player to mute.
     * @return {@code true} if the player is muted.
     */
    public static boolean getIsMuted(@NotNull UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("muted");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Mutes an SPS user in the database (stops them from talking)
     * @param SPSUser The SPS name of the player to mute.
     * @return {@code true} if no errors occurred.
     */
    public static boolean setMutePlayer(@NotNull String SPSUser, boolean muted) {
        String spsEmail = SPSUser + "@seattleschools.org";

        SPSSpigot.logger().log(Level.INFO, "Muting/Unmuting player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("muted", muted);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // mute the player in the database
        try {
            userCol.updateOne(new Document("oAuthEmail", spsEmail), setQuery);

            // mute them on the server
            OfflinePlayer oplayer = SPSSpigot.server().getOfflinePlayer(UUID.fromString(userCol.find(new Document("oAuthEmail", spsEmail)).first().getString("mcUUID")));
            if (muted) {
                SPSSpigot.plugin().mutedPlayers.add(oplayer.getUniqueId());
            } else {
                SPSSpigot.plugin().mutedPlayers.remove(oplayer.getUniqueId());
            }

            return true;
        } catch (MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong muting/unmuting a player!");
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
     */
    public static void registerPlayer(@NotNull UUID uuid, @NotNull String SPSid, @NotNull String name, @NotNull String SPSemail, @NotNull String SPSname) {
        // set UUID and Name
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("oAuthId", SPSid);
        updateFields.append("oAuthEmail", SPSemail);
        updateFields.append("oAuthName", SPSname);

        updateFields.append("mcUUID", uuid.toString());
        updateFields.append("mcName", name);
        updateFields.append( "banned", false);
        updateFields.append("muted", false);

        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        UpdateOptions options = new UpdateOptions().upsert(true);

        // update in the database
        userCol.updateOne(new Document("oAuthId", SPSid), setQuery, options);
        String email = userCol.find(new Document("oAuthId", SPSid)).first().getString("oAuthEmail");

        // get player
        Player player = SPSSpigot.server().getPlayer(uuid);

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

        if (isBanned(uuid)) {
            player.kickPlayer("The SPS account you linked has been banned!");
        }

        // give starting boat
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            SPSSpigot.plugin().giveStartingItems(player);
        }

        ClaimBoard.addBoard(player);

        CompassThread compass = new CompassThread(player, SPSSpigot.getWorldGroup(player.getWorld()));
        SPSSpigot.plugin().compassThreads.put(player.getUniqueId(), compass);
        compass.start();

        player.sendMessage("You've spawned in the lobby, please use the included " + ChatColor.BLUE +"Starting Boat" + ChatColor.WHITE + " to leave the island!");

    }
}
