package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

public class MockPlugin extends JavaPlugin {
    public MockPlugin()
    {
        super();
    }

    protected MockPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
    {
        super(loader, description, dataFolder, file);
    }
}
