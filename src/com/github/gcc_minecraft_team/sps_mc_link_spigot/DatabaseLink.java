package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import com.mongodb.client.result.UpdateResult;
import com.sun.jdi.IntegerValue;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.haoshoku.nick.api.NickAPI;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;

public class DatabaseLink {

    public static final String DBFILE = "databaseConfig.yml";
    public static final String CFGDB = "dbDB";
    public static final String CFGURI = "dbURI";

    // mongo variables
    private static FileConfiguration dbConfig;
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    private static MongoCollection<Document> userCol;
    private static MongoCollection<Team> teamCol;

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
            teamCol = mongoDatabase.getCollection("teams", Team.class);
        } catch(MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong connecting to the MongoDB database, is " + DBFILE + " set up correctly?");
        }
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

    /***
     * Adds a new team to the database
     * @param team
     */
    public static void addTeam(Team team) {
        teamCol.insertOne(team);
    }

    /***
     * Updates an existing team in the database
     * @param team
     */
    public static void updateTeam(Team team) {
        teamCol.updateOne(new BasicDBObject("leader", team.getLeader()), new BasicDBObject("$set", team));
    }

    /**
     * Gets a team from the database
     * @param team
     * @return
     */
    public static Team getTeam(Team team) {
        return teamCol.find(new BasicDBObject("leader", team.getLeader())).first();
    }

    /**
     * Gets all teams in the database
     * @return
     */
    public static Set<Team> getTeams() {
        FindIterable<Team> teams = teamCol.find();
        Set<Team> output = new HashSet<>();
        for (Team team : teams) {
            output.add(team);
        }
        return output;
    }

    /***
     * Removes a team from the database
     * @param team
     */
    public static void removeTeam(Team team) {
        teamCol.deleteOne(new BasicDBObject("leader", team.getLeader()));
    }

    /**
     * saves claims to the database
     * @param claims
     */
    public static void saveClaims(@NotNull Map<UUID, Set<Chunk>> claims) {

        for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
            BasicDBObject updateFields = new BasicDBObject();

            // load claimed chunks for the player
            ArrayList<ArrayList<Integer>> claimList = new ArrayList<ArrayList<Integer>>();
            for (Chunk cChunk : player.getValue()) {
                ArrayList<Integer> cCoords = new ArrayList<>();
                cCoords.add(cChunk.getX());
                cCoords.add(cChunk.getZ());
                claimList.add(cCoords);
            }

            // add it to the update queue
            updateFields.append("claims", claimList);

            // set query
            BasicDBObject setQuery = new BasicDBObject();
            setQuery.append("$set", updateFields);

            // upsert means it will create a new field if the player has never claimed land before
            UpdateOptions options = new UpdateOptions().upsert(true);

            // push to the database
            userCol.updateOne(new Document("mcUUID", player.getKey().toString()), setQuery, options);
        }
    }

    /***
     * Gets claims from the database
     * @return
     */
    @NotNull
    public static Map<UUID, Set<Chunk>> getClaims() {
        Map<UUID, Set<Chunk>> output = new HashMap<>();
        FindIterable<Document> allDocs = userCol.find();
        for(Document doc : allDocs) {
            UUID uuid = UUID.fromString(doc.getString("mcUUID"));
            if (doc.containsKey("claims")) {
                ArrayList claimList = (ArrayList) doc.get("claims");
                Set<Chunk> chunksSet = new HashSet<>();
                if (claimList != null) {
                    for (Object chunkCoords : claimList) {
                        ArrayList<Integer> cc = (ArrayList<Integer>) chunkCoords;
                        Chunk c = SPSSpigot.server().getWorlds().get(0).getChunkAt(cc.get(0), cc.get(1));
                        if (c != null) {
                            chunksSet.add(c);
                        }
                    }
                }
                if (!chunksSet.isEmpty()) {
                    output.put(uuid, chunksSet);
                }
            }
        }
        return output;
    }

    /**
     * Gets the MC names of all SPS users registered
     * @return A {@link List} of all the names.
     */
    @NotNull
    public static List<String> getAllSPSNames() {
        List<String> spsNames = new ArrayList<String>();
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
    public static Player getSPSPlayer(@NotNull String SPSName) {
        UUID uuid = getSPSUUID(SPSName);
        if (uuid != null)
            return SPSSpigot.server().getPlayer(uuid);
        else
            return null;
    }

    /**
     * Checks if a player is banned.
     * @param uuid The Minecraft {@link UUID} of the player to ban.
     * @return {@code true} if the player is banned.
     **/
    public static boolean getIsBanned(@NotNull UUID uuid) {
        try {
            return userCol.find(new Document("mcUUID", uuid.toString())).first().getBoolean("banned");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Bans a player using their SPS ID
     * @param SPSUser The SPS username to ban without domain (e.g. 1absmith)
     * @return {@code true} if the SPS ID was successfully banned.
    **/
    public static boolean banPlayer(@NotNull String SPSUser) {
        String spsEmail = SPSUser + "@seattleschools.org";

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
     * Registers a new player in the database.
     * @param uuid The Minecraft {@link UUID} of the player.
     * @param SPSid The SPS ID of the player.
     * @param name The new name of the player.
     **/
    public static void registerPlayer(@NotNull UUID uuid, @NotNull String SPSid, @NotNull String name) {
        // set UUID and Name
        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("mcUUID", uuid.toString());
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

        if (getIsBanned(uuid)) {
            player.kickPlayer("The SPS account you linked has been banned!");
        }

        // give starting boat
        player.getInventory().setItemInMainHand(new ItemStack(Material.OAK_BOAT));
        SPSSpigot.showBoard(player);

        // claim map
        BukkitScheduler scheduler = SPSSpigot.server().getScheduler();
        scheduler.scheduleSyncRepeatingTask(SPSSpigot.plugin(), new Runnable() {
            @Override
            public void run() {
                // compass
                String claimStatus = net.md_5.bungee.api.ChatColor.DARK_GREEN + "Wilderness";
                UUID chunkOwner = SPSSpigot.claims().getChunkOwner(player.getLocation().getChunk());
                if (chunkOwner != null) {
                    claimStatus = net.md_5.bungee.api.ChatColor.RED + DatabaseLink.getSPSName(chunkOwner);
                }

                SPSSpigot.claims().updateClaimMap(player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append("[" + SPSSpigot.getCardinalDirection(player) + "] " + claimStatus).create());
            }
        }, 0, 10);

        player.sendMessage("You've spawned in the lobby, please use the included " + ChatColor.BLUE +"Starting Boat" + ChatColor.WHITE + " to leave the island!");

    }

}
