package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.general.GeneralCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.WorldGroupCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.WorldGroupTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap.MapEvents;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SPSSpigot extends JavaPlugin {

    public PermissionsHandler perms;
    private Set<WorldGroup> worldGroups;
    public final Map<UUID, FastBoard> boards = new HashMap<>();

    @Override
    public void onEnable() {
        // Load plugin config
        PluginConfig.LoadConfig();

        // Setup Database
        DatabaseLink.SetupDatabase();

        // Start listen server
        WebInterfaceLink.Listen();

        // Setup Permissions
        ConfigurationSerialization.registerClass(Rank.class);

        perms = new PermissionsHandler();
        getServer().getPluginManager().registerEvents(new PermissionsEvents(), this);

        // Setup Claims
        ConfigurationSerialization.registerClass(Team.class);

        worldGroups = DatabaseLink.getWorldGroups();
        getServer().getPluginManager().registerEvents(new ClaimEvents(), this);

        // register chat events
        getServer().getPluginManager().registerEvents(new ChatEvents(), this);

        // map events
        getServer().getPluginManager().registerEvents(new MapEvents(), this);

        ClaimCommands claimCommands = new ClaimCommands();
        ClaimTabCompleter claimTabCompleter = new ClaimTabCompleter();

        this.getCommand("claim").setExecutor(claimCommands);
        this.getCommand("claim").setTabCompleter(claimTabCompleter);

        this.getCommand("unclaim").setExecutor(claimCommands);
        this.getCommand("unclaim").setTabCompleter(claimTabCompleter);

        this.getCommand("team").setExecutor(new TeamCommands());
        this.getCommand("team").setTabCompleter(new TeamTabCompleter());

        this.getCommand("perms").setExecutor(new PermissionsCommands());
        this.getCommand("perms").setTabCompleter(new PermissionsTabCompleter());

        this.getCommand("mod").setExecutor(new ModerationCommands());
        this.getCommand("mod").setTabCompleter(new ModerationTabCompleter());

        this.getCommand("wgroup").setExecutor(new WorldGroupCommands());
        this.getCommand("wgroup").setTabCompleter(new WorldGroupTabCompleter());

        // General utility commands for players
        GeneralCommands generalCommands = new GeneralCommands();
        this.getCommand("spawn").setExecutor(generalCommands);
        this.getCommand("cancel").setExecutor(generalCommands);

        // Moderation discord integration
        DiscordCommands discordCommands = new DiscordCommands();
        DiscordTabCompleter discordTabCompleter = new DiscordTabCompleter();

        this.getCommand("report").setExecutor(discordCommands);
        this.getCommand("report").setTabCompleter(discordTabCompleter);

        this.getCommand("modmail").setExecutor(discordCommands);
        this.getCommand("modmail").setTabCompleter(discordTabCompleter);

        // Setup other stuff
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(), this);
        SPSSpigot.logger().log(Level.INFO, "SPS Spigot integration started.");

        System.out.println("==[SPS MC INITIALIZED SUCCESSFULLY]==");
    }

    @Override
    public void onDisable() {

    }

    /**
     * Getter for a world group.
     * @param name The name of the world group.
     * @return The world group's {@link WorldGroup}.
     */
    @Nullable
    public WorldGroup getWorldGroup(@NotNull String name) {
        for (WorldGroup worldGroup : worldGroups) {
            if (worldGroup.getName().equalsIgnoreCase(name))
                return worldGroup;
        }
        return null;
    }

    /**
     * Getter for a world group.
     * @param world The {@link World} whose worldGroup {@link WorldGroup} should be found.
     * @return The world group's {@link WorldGroup}, or {@code null} if the {@link World} is not in a world group.
     */
    @Nullable
    public static WorldGroup getWorldGroup(@NotNull World world) {
        for (WorldGroup worldGroup : plugin().worldGroups) {
            if (worldGroup.hasWorld(world))
                return worldGroup;
        }
        return null;
    }

    /**
     * Gets all world groups.
     * @return An unmodifiable {@link Set} of each world group's {@link WorldGroup}.
     */
    @NotNull
    public Set<WorldGroup> getWorldGroups() {
        return Collections.unmodifiableSet(worldGroups);
    }

    /**
     * Adds a new world group.
     * @param worldGroup The {@link WorldGroup} of the world group to add.
     * @return {@code true} if successful; {@code false} if a world group already exists with this name.
     */
    public boolean addWorldGroup(@NotNull WorldGroup worldGroup) {
        if (getWorldGroup(worldGroup.getName()) == null) {
            worldGroups.add(worldGroup);
            DatabaseLink.addWorldGroup(worldGroup);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a world group.
     * @param worldGroup The {@link WorldGroup} of the world group to remove.
     * @return {@code true} if successful; {@code false} if this world group is unknown.
     */
    public boolean removeWorldGroup(@NotNull WorldGroup worldGroup) {
        if (worldGroups.remove(worldGroup)) {
            DatabaseLink.removeWorldGroup(worldGroup);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets a chat friendly colored {@link String} of player {@link Rank}s.
     * @param player The {@link UUID} of the player to create the tag for.
     * @return The text of the tag.
     */
    @NotNull
    public String getRankTag(@NotNull UUID player) {
        StringBuilder rankTag = new StringBuilder();
        if (perms().getPlayerRanks(player).size() > 0) {
            for (Rank rank : perms().getPlayerRanks(player)) {
                rankTag.append(rank.getColor()).append("[").append(rank.getName()).append("]").append(ChatColor.WHITE);
            }
        }
        return rankTag.toString();
    }

    /**
     * Gets a chat-friendly {@link String} of player {@link Rank}s.
     * @param player The {@link UUID} of the player to create the tag for.
     * @return The unformatted text of the tag.
     */
    @NotNull
    public String getRankTagNoFormat(@NotNull UUID player) {
        StringBuilder rankTag = new StringBuilder();
        if (perms().getPlayerRanks(player).size() > 0) {
            for (Rank rank : perms().getPlayerRanks(player)) {
                rankTag.append("[").append(rank.getName()).append("]");
            }
        }
        return rankTag.toString();
    }

    /**
     * Gets the cardinal direction that the {@link Player} is currently facing.
     * @param player The {@link Player} to check.
     * @return A one or two-character {@link String}: N, NE, E, SE, S, SW, W, or NW.
     */
    public static String getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "N";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        } else if (337.5 <= rotation && rotation < 360.0) {
            return "N";
        } else {
            return null;
        }
    }

    /**
     * Creates a board for a {@link Player}.
     * @param player The {@link Player} whose board should be created.
     */
    public static void showBoard(Player player) {
        // create the board
        FastBoard board = new FastBoard(player);
        board.updateTitle(net.md_5.bungee.api.ChatColor.BOLD + "[N]");
        SPSSpigot.plugin().boards.put(player.getUniqueId(), board);
    }

    /**
     * Convenience function to get this {@link JavaPlugin}.
     * @return This {@link JavaPlugin}.
     */
    public static SPSSpigot plugin() {
        return JavaPlugin.getPlugin(SPSSpigot.class);
    }

    /**
     * Convenience function to get the {@link Server}.
     * @return This {@link Server}.
     */
    public static Server server() {
        return plugin().getServer();
    }

    /**
     * Convenience function to get the {@link PermissionsHandler}.
     * @return This plugin's {@link PermissionsHandler}.
     */
    public static PermissionsHandler perms() {
        return plugin().perms;
    }

    /**
     * Convenience function to get the {@link Server}'s {@link Logger}.
     * @return The {@link Server}'s {@link Logger}.
     */
    public static Logger logger() {
        return plugin().getLogger();
    }
}
