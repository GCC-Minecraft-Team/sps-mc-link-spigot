package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationCommands;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation.ModerationTabCompleter;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.*;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SPSSpigot extends JavaPlugin {

    public PermissionsHandler perms;

    @Override
    public void onEnable() {
        // Load plugin config
        PluginConfig.LoadConfig();

        // Setup Databse
        DatabaseLink.SetupDatabase();

        // Start listen server
        WebInterfaceLink.Listen();

        // Setup Permissions
        ConfigurationSerialization.registerClass(Rank.class);

        perms = new PermissionsHandler();
        getServer().getPluginManager().registerEvents(new PermissionsEvents(), this);

        // register chat events
        getServer().getPluginManager().registerEvents(new ChatEvents(), this);

        this.getCommand("perms").setExecutor(new PermissionsCommands());
        this.getCommand("perms").setTabCompleter(new PermissionsTabCompleter());

        this.getCommand("mod").setExecutor(new ModerationCommands());
        this.getCommand("mod").setTabCompleter(new ModerationTabCompleter());

        // Setup other stuff
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        SPSSpigot.logger().log(Level.INFO, "SPS Spigot integration started.");
    }

    @Override
    public void onDisable() {

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
