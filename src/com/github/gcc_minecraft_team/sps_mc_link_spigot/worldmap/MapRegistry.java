package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.PluginConfig;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapRegistry {

    private static final String MAPFILE = "mapConfig.yml";
    private static final String CFGMAPS = "maps";

    public static FileConfiguration mapCfg;

    public static Map<Integer, CustomMap> maps;

    public static void initConfig() {

        maps = new HashMap<>();

        ConfigurationSerialization.registerClass(PlayerMap.class);
        ConfigurationSerialization.registerClass(ImageMap.class);
        ConfigurationSerialization.registerClass(ClaimMap.class);

        SPSSpigot.plugin().saveResource(MAPFILE, false);
        mapCfg = YamlConfiguration.loadConfiguration(new File(SPSSpigot.plugin().getDataFolder(), MAPFILE));
        mapCfg.addDefault(CFGMAPS, new HashMap<String, CustomMap>());
        loadConfig();
    }

    public static void loadConfig() {
        try {
            mapCfg.load(new File(SPSSpigot.plugin().getDataFolder(), MAPFILE));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        MemorySection mapsSection = (MemorySection) mapCfg.get(CFGMAPS);
        maps = new HashMap<>();
        for (String key : mapsSection.getKeys(false)) {
            int mapID = Integer.parseInt(key);
            // We need to check that this ID still exists, otherwise it could be taken by a new map that shouldn't be custom.
            if (SPSSpigot.server().getMap(mapID) != null) {
                maps.put(mapID, (CustomMap) mapsSection.get(key));
                MapEvents.initMap(mapID);
            }
        }

        SPSSpigot.server().getPluginManager().registerEvents(new MapEvents(), SPSSpigot.plugin());
    }

    public static void saveConfig() {
        HashMap<String, CustomMap> mapsMap = new HashMap<>();
        for (Map.Entry<Integer, CustomMap> entry : maps.entrySet())
            mapsMap.put(entry.getKey().toString(), entry.getValue());
        mapCfg.set(CFGMAPS, mapsMap);
        try {
            mapCfg.save(new File(SPSSpigot.plugin().getDataFolder(), MAPFILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the map ID from an {@link ItemStack}.
     * @param item The {@link ItemStack}.
     * @return The map ID, or -1 if invalid.
     */
    static public int getMapIdFromItemStack(@NotNull final ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() instanceof MapMeta) {
            MapMeta meta = (MapMeta) item.getItemMeta();
            if (meta.getMapView() != null)
                return meta.getMapView().getId();
        }
        return -1;
    }

    public static void generatePlayerMap(@NotNull Player player, int mapsX, int mapsZ) {
        for (int z = 0; z < mapsZ; z++) {
            for (int x = 0; x < mapsX; x++) {
                ItemStack item = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) item.getItemMeta();
                MapView view = SPSSpigot.server().createMap(player.getWorld());
                maps.put(view.getId(), new PlayerMap((int) ((x - mapsX/2.0) * 2048), (int) ((z - mapsZ/2.0) * 2048)));
                view.getRenderers().clear();
                view.addRenderer(new PlayerMapRenderer((MapRegistry.PlayerMap) maps.get(view.getId())));
                meta.setMapView(view);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }
        saveConfig();
    }

    public static void generateImageMap(@NotNull Player player, @NotNull String file) {
        int mapsX;
        int mapsY;
        try {
            BufferedImage image = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(), file));
            mapsX = (int) Math.ceil(image.getWidth() / 128.0);
            mapsY = (int) Math.ceil(image.getHeight() / 128.0);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (int y = 0; y < mapsY; y++) {
            for (int x = 0; x < mapsX; x++) {
                ItemStack item = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) item.getItemMeta();
                MapView view = SPSSpigot.server().createMap(player.getWorld());
                maps.put(view.getId(), new ImageMap(x * 128, y * 128, file));
                view.getRenderers().clear();
                view.addRenderer(new ImageMapRenderer((MapRegistry.ImageMap) maps.get(view.getId())));
                meta.setMapView(view);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }
        saveConfig();
    }

    public static boolean isClaimMap(@Nullable ItemStack stack) {
        if (stack != null &&  PluginConfig.isClaimMapEnabled()) {
            if (stack.hasItemMeta() && stack.getItemMeta() instanceof MapMeta) {
                MapMeta meta = (MapMeta) stack.getItemMeta();
                MapView view = meta.getMapView();
                return view != null && maps.containsKey(view.getId()) && maps.get(view.getId()) instanceof ClaimMap;
            }
        }
        return false;
    }

    public static void generateClaimMap(@NotNull Player player) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();
        MapView view = SPSSpigot.server().createMap(player.getWorld());
        maps.put(view.getId(), new ClaimMap());
        view.getRenderers().clear();
        view.addRenderer(new ClaimMapRenderer());
        meta.setMapView(view);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
        saveConfig();
    }

    public enum CustomMapType {
        PLAYER, IMAGE, CLAIMS
    }

    public static abstract class CustomMap implements ConfigurationSerializable {
    }

    public static class PlayerMap extends CustomMap {

        private static final String XOFFSET = "xOffset";
        private static final String ZOFFSET = "zOffset";

        public final int xOffset;
        public final int zOffset;

        public PlayerMap(int xOffset, int zOffset) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
        }

        public PlayerMap(@NotNull Map<String, Object> map) {
            xOffset = (int) map.get(XOFFSET);
            zOffset = (int) map.get(ZOFFSET);
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put(XOFFSET, xOffset);
            map.put(ZOFFSET, zOffset);
            return map;
        }
    }

    public static class ImageMap extends CustomMap {

        private static final String XOFFSET = "xOffset";
        private static final String YOFFSET = "yOffset";
        private static final String FILENAME = "file";

        public final int xOffset;
        public final int yOffset;
        public final String file;

        public ImageMap(int xOffset, int yOffset, String file) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.file = file;
        }

        public ImageMap(@NotNull Map<String, Object> map) {
            xOffset = (int) map.get(XOFFSET);
            yOffset = (int) map.get(YOFFSET);
            file = (String) map.get(FILENAME);
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put(XOFFSET, xOffset);
            map.put(YOFFSET, yOffset);
            map.put(FILENAME, file);
            return map;
        }
    }

    public static class ClaimMap extends CustomMap {

        public ClaimMap() {

        }

        public ClaimMap(@NotNull Map<String, Object> map) {

        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            return new HashMap<>();
        }
    }
}
