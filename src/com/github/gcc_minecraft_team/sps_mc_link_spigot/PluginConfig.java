package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;

public class PluginConfig {
    public static final String PLFILE = "pluginConfig.yml";
    public static final String JWTSECRET = "jwtSecret";
    public static final String WEBAPPURL = "webAppURL";
    public static final String PLUGINMOTD = "pluginMOTD";
    public static final String REPORTWEBHOOK = "reportWebhook";
    public static final String MESSAGEWEBHOOK = "messageWebhook";

    private static FileConfiguration pluginCfg;

    /**
     * Loads the plugin's {@link FileConfiguration} from {@value PLFILE}.
     */
    public static void LoadConfig() {
        // create config if it doesn't exist
        SPSSpigot.plugin().saveResource(PLFILE, false);
        pluginCfg = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
        pluginCfg.addDefault(JWTSECRET, "");

        // load config
        try {
            pluginCfg.load(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // V get functions V

    /**
     * Gets the JWT secret from the {@value JWTSECRET} in {@value PLFILE} or randomly generates and saves one if it does not exist.
     * @return The JWT secret key.
     */
    public static String GetJWTSecret() {
        String key = "";
        if (pluginCfg != null) {
            key = (String) pluginCfg.get(JWTSECRET);
        } else {
            SPSSpigot.plugin().saveResource(PLFILE, false);
            pluginCfg = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
        }
        // If the key doesn't exist, generate a random one.
        if (key.equals("")) {
            char[] chars = new char[16];
            Random r = new Random();
            for (int i = 0; i < 16; i++) {
                // Start at index 32 and end at 126 to exclude non-printing characters (0-31, 127)
                chars[i] = (char) (r.nextInt(127-32) + 32);
            }
            key = new String(chars);
            pluginCfg.set(JWTSECRET, key);
            SPSSpigot.logger().log(Level.INFO, "Generated new JWT secret key: " + pluginCfg.get(JWTSECRET));
            try {
                pluginCfg.save(new File(SPSSpigot.plugin().getDataFolder(), PLFILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    /**
     * Gets the URL we should listen on for the web app from {@value WEBAPPURL} in {@value PLFILE}.
     * @return The URL to listen on for the web app.
     */
    public static String GetWebAppURL() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(WEBAPPURL);
        } else {
            return "";
        }
    }

    /**
     * Gets the plugin MOTD from {@value PLUGINMOTD} in {@value PLFILE}.
     * @return The MOTD.
     */
    public static String getPluginMOTD() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(PLUGINMOTD);
        } else {
            return "";
        }
    }

    /**
     * Gets the plugin MOTD from {@value REPORTWEBHOOK} in {@value PLFILE}.
     * @return The Report Webhook URL.
     */
    public static String GetReportWebhook() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(REPORTWEBHOOK);
        } else {
            return "";
        }
    }

    /**
     * Gets the plugin MOTD from {@value MESSAGEWEBHOOK} in {@value PLFILE}.
     * @return The Report Webhook URL.
     */
    public static String GetMessageWebhook() {
        if (pluginCfg != null) {
            return (String) pluginCfg.get(MESSAGEWEBHOOK);
        } else {
            return "";
        }
    }
}
