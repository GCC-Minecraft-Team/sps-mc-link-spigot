package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class SPSSpigot extends JavaPlugin {

    public PermissionsHandler perms;

    @Override
    public void onEnable() {
        // Setup Permissions
        perms = new PermissionsHandler();
        getServer().getPluginManager().registerEvents(new PermissionsEvents(), this);

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
