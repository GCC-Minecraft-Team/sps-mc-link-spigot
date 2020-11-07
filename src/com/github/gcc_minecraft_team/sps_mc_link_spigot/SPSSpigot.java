package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.general.GeneralCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap.MapEvents;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SPSSpigot extends JavaPlugin {

    public PermissionsHandler perms;
    public ClaimHandler claims;
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

        claims = new ClaimHandler();
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
        SPSSpigot.logger().log(Level.INFO, "SPS Spigot integration started.");

        System.out.println("==[SPS MC INITIALIZED SUCCESSFULLY]==");
    }

    @Override
    public void onDisable() {

    }

    /**
     * Gets a chat friendly colored string of player ranks
     * @param p player
     * @return string with ranks
     */
    public static String GetRankTag(Player p) {
        // set rank tag formatting
        StringBuilder rankTag = new StringBuilder();
        if (SPSSpigot.perms().getPlayerRanks(p.getUniqueId()).size() > 0) {
            for (Rank rank : SPSSpigot.perms().getPlayerRanks(p.getUniqueId())) {
                rankTag.append(rank.getColor() + "[" + rank.getName() +"]" + ChatColor.WHITE);
            }
        }

        return rankTag.toString();
    }

    public static String GetRankTagNoFormat(Player p) {
        // set rank tag without color formatting
        StringBuilder rankTag = new StringBuilder();
        if (SPSSpigot.perms().getPlayerRanks(p.getUniqueId()).size() > 0) {
            for (Rank rank : SPSSpigot.perms().getPlayerRanks(p.getUniqueId())) {
                rankTag.append("[" + rank.getName() +"]");
            }
        }

        return rankTag.toString();
    }

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
     * Convenience function to get the {@link ClaimHandler}.
     * @return This plugin's {@link ClaimHandler}.
     */
    public static ClaimHandler claims() {
        return plugin().claims;
    }

    /**
     * Convenience function to get the {@link Server}'s {@link Logger}.
     * @return The {@link Server}'s {@link Logger}.
     */
    public static Logger logger() {
        return plugin().getLogger();
    }
}
