package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
        System.out.println("SPS Spigot integration started.");
    }

    @Override
    public void onDisable() {

    }

    public static SPSSpigot plugin() {
        return JavaPlugin.getPlugin(SPSSpigot.class);
    }
}
