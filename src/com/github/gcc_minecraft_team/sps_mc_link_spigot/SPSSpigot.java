package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.general.GeneralCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.*;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap.MapEvents;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SPSSpigot extends JavaPlugin {

    public PermissionsHandler perms;
    public ClaimHandler claims;

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

        this.getCommand("claim").setExecutor(new ClaimCommands());
        this.getCommand("claim").setTabCompleter(new ClaimTabCompleter());

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
