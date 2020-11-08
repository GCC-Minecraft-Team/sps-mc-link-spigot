package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class PlayerRenderer extends MapRenderer {

    private static final int UPDATE_DELAY = 5;

    private static final Biome[] B_WATER = {
            Biome.OCEAN,
            Biome.COLD_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
    };

    private boolean hasRendered;
    private int offsetX;
    private int offsetZ;

    private BufferedImage background;
    private BufferedImage frame;

    /**
     * Sets the map offset in blocks.
     * @param x The X offset.
     * @param z The Z offset.
     */
    public void setOffest(int x, int z) {
        offsetX = x;
        offsetZ = z;
    }

    /**
     * Checks if a block is within the spawn protection radius.
     * @param x The X offset.
     * @param z The Z offset.
     */
     private boolean CheckInSpawnRadius(int x, int z) {
         double zdist = z - SPSSpigot.server().getWorlds().get(0).getSpawnLocation().getZ();
         double xdist = x - SPSSpigot.server().getWorlds().get(0).getSpawnLocation().getX();
         return Math.abs(zdist) <= SPSSpigot.server().getSpawnRadius() && Math.abs(xdist) <= SPSSpigot.server().getSpawnRadius();
    }

    @Override
    public void initialize(@NotNull MapView map) {
         // load map frame
        try {
            frame = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(),"frame.png")).getSubimage((offsetX / 16) + 128, (offsetZ / 16) + 128, 128, 128);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

         // generate images
        try {
            GenerateWorldBackground();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // load generated images
        try {
            background = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(),"worldCache" + offsetZ + offsetX +".png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // re-renders map
        BukkitScheduler scheduler = SPSSpigot.server().getScheduler();
        scheduler.scheduleSyncRepeatingTask(SPSSpigot.plugin(), () -> hasRendered = false, 100L, UPDATE_DELAY * 20);

        super.initialize(map);
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        // render map background
        if (!hasRendered) {
            Collection<Player> onlinePlayers = (Collection<Player>) SPSSpigot.server().getOnlinePlayers();
            World world = SPSSpigot.server().getWorlds().get(0);

            // load background cache
            canvas.drawImage(0, 0, background);

            // loops through map pixels
            // TODO: cache the world data
            for (int x = 0; x < 128; x++) {
                for (int z = 0; z < 128; z++) {
                    Location spawn = world.getSpawnLocation();

                    // player detection
                    for(Player p : onlinePlayers) {
                        try {
                            if (p.getLocation().getX() < (((x + 1) * 16) + offsetX) + 32 && p.getLocation().getX() >= (((x + 1) * 16) + offsetX) - 16) {
                                if (p.getLocation().getZ() < (((z + 1) * 16) + offsetZ) + 32 && p.getLocation().getZ() >= (((z + 1) * 16) + offsetZ) - 16) {
                                    canvas.setPixel(x, z, MapPalette.matchColor(255, 255, 255));
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }
        hasRendered = true;
    }

    /**
     * Generates the image used as the background of the map
     */
    private void GenerateWorldBackground() throws IOException {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        World world = SPSSpigot.server().getWorlds().get(0);

        // generate background
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                Location spawn = world.getSpawnLocation();
                Block block = world.getBlockAt((int) spawn.getX() + (x * 16) + offsetX, 23, (int) spawn.getZ() + (z * 16) + offsetZ);
                Biome biome = block.getBiome();
                if (Arrays.asList(B_WATER).contains(biome)) {
                    g2d.setColor(new Color(25, 0, 120));
                    g2d.fillRect(x, z, 1, 1);
                } else if (biome.equals(Biome.MUSHROOM_FIELDS) || biome.equals(Biome.MUSHROOM_FIELD_SHORE)) {
                    if (CheckInSpawnRadius((int) spawn.getX() + (x * 16) + offsetX, (int) spawn.getZ() + (z * 16) + offsetZ)) {
                        g2d.setColor(new Color(255, 0, 0));
                        g2d.fillRect(x, z, 1, 1);
                    }
                } else {
                    g2d.setColor(new Color(50, 120, 0));
                    g2d.fillRect(x, z, 1, 1);
                }
            }
        }

        g2d.drawImage(frame, 0, 0, null);

        // write background to file
        File file = new File(SPSSpigot.plugin().getDataFolder(),"worldCache" + offsetZ + offsetX +".png");
        ImageIO.write(img, "png", file);
    }
}
