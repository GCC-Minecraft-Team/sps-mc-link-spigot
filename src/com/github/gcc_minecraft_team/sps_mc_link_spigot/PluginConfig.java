package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class PluginConfig {
    public static final String PLFILE = "pluginConfig.yml";
    public static final String JWTSECRET = "jwtSecret";
    public static final String WEBAPPURL = "webAppURL";
    private static FileConfiguration pluginCfg;

    /**
     * loads the plugin configuration file
     */
    public static void LoadConfig() {
        // create config if it doesn't exist
        SPSSpigot.plugin().saveResource(PLFILE, false);
        pluginCfg = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
        pluginCfg.addDefault(JWTSECRET, new String());

        // load config
        try {
            pluginCfg.load(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // V get functions V

    /**
     * gets the JWT secret from the plugin config file
     * @return
     */
    public static String GetJWTSecret() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(JWTSECRET);
        } else {
            return "";
        }
    }

    /**
     * gets the URL we should listen on for the web app
     * @return
     */
    public static String GetWebAppURL() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(WEBAPPURL);
        } else {
            return "";
        }
    }
}
