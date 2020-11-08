package com.github.gcc_minecraft_team.sps_mc_link_spigot.database;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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
     * Creates a connection to the MongoDB com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
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

        // set up info for com.github.gcc_minecraft_team.sps_mc_link_spigot.database
        ConnectionString connectionString = new ConnectionString((String) dbConfig.get(CFGURI));
        String dbName = (String) dbConfig.get(CFGDB);

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        // set client settings
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .codecRegistry(codecRegistry).build();

        // set com.github.gcc_minecraft_team.sps_mc_link_spigot.database and connect
        try {
            mongoClient = MongoClients.create(clientSettings);
            mongoDatabase = mongoClient.getDatabase(dbName);
            userCol = mongoDatabase.getCollection("users");
            teamCol = mongoDatabase.getCollection("teams", Team.class);
        } catch(MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Something went wrong connecting to the MongoDB com.github.gcc_minecraft_team.sps_mc_link_spigot.database, is " + DBFILE + " set up correctly?");
        }
    }

    /**
     * Gets whether a player is registered on the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param uuid The {@link UUID} of the player to check.
     * @return {@code true} if the player is registered.
     */
    public static boolean isRegistered(@NotNull UUID uuid) {
        try {
            // check if player is registered
            return userCol.countDocuments(new Document("mcUUID", uuid.toString())) == 1;
        } catch(MongoException exception) {
            SPSSpigot.logger().log(Level.SEVERE, "Couldn't check user from com.github.gcc_minecraft_team.sps_mc_link_spigot.database! Error: " + exception.toString());
            return false;
        }
    }

    /**
     * Gets all the world groups. Should generally be only called on startup.
     * @return A {@link Set} of the {@link WorldGroup} world groups.
     */
    public static Set<WorldGroup> getWorldGroups() { // FIXME: Doesn't do anything.
        return new HashSet<>();
    }

    /**
     * Adds a world group to the database. This should not be called to update the world group.
     * @param worldGroup The {@link WorldGroup} of the world group.
     * @return {@code true} if successful.
     */
    public static boolean addWorldGroup(WorldGroup worldGroup) { // FIXME: Doesn't do anything
        return true;
    }

    /**
     * Remove a world group from the database.
     * @param worldGroup The {@link WorldGroup} of the world group.
     * @return {@code true} if successful.
     */
    public static boolean removeWorldGroup(WorldGroup worldGroup) { // FIXME: Doesn't do anything
        return true;
    }

    /**
     * Adds a new {@link Team} to the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param team The {@link Team} to add.
     */
    public static void addTeam(@NotNull Team team) { // FIXME: Update this to the new world groups system.
        teamCol.insertOne(team);
    }

    /**
     * Updates an existing {@link Team} in the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param team The {@link Team} to update.
     */
    public static void updateTeam(@NotNull Team team) { // FIXME: Update this to the new world groups system.
        teamCol.updateOne(new BasicDBObject("name", team.getName()), new BasicDBObject("$set", team));
    }

    /**
     * Gets a {@link Team} from the com.github.gcc_minecraft_team.sps_mc_link_spigot.database. Unlikely to be used?
     * @param team The {@link Team} to get.
     * @return The team.
     */
    public static Team getTeam(@NotNull Team team) { // FIXME: Update this to the new world groups system.
        return teamCol.find(new BasicDBObject("name", team.getName())).first();
    }

    /**
     * Gets all {@link Team}s in the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param worldGroup The {@link WorldGroup} worldGroup from which to find the {@link Team}.
     * @return A {@link Set} of {@link Team}s found.
     */
    public static Set<Team> getTeams(@NotNull WorldGroup worldGroup) { // FIXME: Update this to the new world groups system.
        FindIterable<Team> teams = teamCol.find();
        Set<Team> output = new HashSet<>();
        for (Team team : teams) {
            output.add(team);
        }
        return output;
    }

    /**
     * Removes a {@link Team} from the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param team The {@link Team} to remove.
     */
    public static void removeTeam(@NotNull Team team) { // FIXME: Update this to the new world groups system.
        teamCol.deleteOne(new BasicDBObject("name", team.getName()));
    }

    /**
     * Saves getWorldGroup to the database.
     * @param claims The claim map to save.
     * @param worldGroup The {@link WorldGroup} worldGroup to which to save the claims.
     */
    public static void saveClaims(@NotNull Map<UUID, Set<Chunk>> claims, @NotNull WorldGroup worldGroup) { // FIXME: Update this to the new world groups system.

        for (Map.Entry<UUID, Set<Chunk>> player : claims.entrySet()) {
            BasicDBObject updateFields = new BasicDBObject();

            // load claimed chunks for the player
            ArrayList<ArrayList<Integer>> claimList = new ArrayList<>();
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

            // push to the com.github.gcc_minecraft_team.sps_mc_link_spigot.database
            userCol.updateOne(new Document("mcUUID", player.getKey().toString()), setQuery, options);
        }
    }

    /**
     * @param worldGroup The {@link WorldGroup} worldGroup from which to get claims.
     * Gets getWorldGroup from the database.
     * @return The worldGroup's claim map.
     */
    @NotNull
    public static Map<UUID, Set<Chunk>> getClaims(@NotNull WorldGroup worldGroup) { // FIXME: Update this to the new world groups system.
        Map<UUID, Set<Chunk>> output = new HashMap<>();
        FindIterable<Document> allDocs = userCol.find();
        for(Document doc : allDocs) {
            UUID uuid = UUID.fromString(doc.getString("mcUUID"));
            if (doc.containsKey("claims")) {
                ArrayList<Object> claimList = (ArrayList<Object>) doc.get("claims");
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
     * Gets the MC names of all SPS users registered.
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
     */
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
     */
    public static boolean banPlayer(@NotNull String SPSUser) {
        String spsEmail = SPSUser + "@seattleschools.org";

        SPSSpigot.logger().log(Level.INFO, "Banning player with SPS email: " + spsEmail);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("banned", true);
        // set query
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);

        // ban the player in the com.github.gcc_minecraft_team.sps_mc_link_spigot.database
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
     * Registers a new player in the com.github.gcc_minecraft_team.sps_mc_link_spigot.database.
     * @param uuid The Minecraft {@link UUID} of the player.
     * @param SPSid The SPS ID of the player.
     * @param name The new name of the player.
     */
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

        // update in the com.github.gcc_minecraft_team.sps_mc_link_spigot.database
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
        scheduler.scheduleSyncRepeatingTask(SPSSpigot.plugin(), () -> {
            // compass
            String claimStatus = net.md_5.bungee.api.ChatColor.DARK_GREEN + "Wilderness";
            UUID chunkOwner = SPSSpigot.getWorldGroup(player.getWorld()).getChunkOwner(player.getLocation().getChunk());
            if (chunkOwner != null) {
                claimStatus = net.md_5.bungee.api.ChatColor.RED + DatabaseLink.getSPSName(chunkOwner);
            }

            SPSSpigot.getWorldGroup(player.getWorld()).updateClaimMap(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append("[" + SPSSpigot.getCardinalDirection(player) + "] " + claimStatus).create());
        }, 0, 10);

        player.sendMessage("You've spawned in the lobby, please use the included " + ChatColor.BLUE +"Starting Boat" + ChatColor.WHITE + " to leave the island!");

    }

}
