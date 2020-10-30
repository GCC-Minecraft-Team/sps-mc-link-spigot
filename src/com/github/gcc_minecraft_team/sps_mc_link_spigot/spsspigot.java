package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.plugin.java.JavaPlugin;

public class SPSSpigot extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        System.out.println("SPS Spigot integration started.");
    }

    @Override
    public void onDisable() {

    }
}
