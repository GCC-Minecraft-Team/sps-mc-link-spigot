package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SignboardRenderer extends MapRenderer {

    private boolean hasRendered;
    private int offsetX;
    private int offsetZ;

    private BufferedImage image;

    /**
     * Sets the map offset in blocks
     * @param x
     * @param z
     */
    public void setOffest(int x, int z) {
        offsetX = x;
        offsetZ = z;
    }

    /**
     * Loads an image from the plugin data directory
     * @param filename
     * @throws IOException
     */
    public void loadIamge(String filename) throws IOException {
        image = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(), filename));
    }

    @Override
    public void initialize(@NotNull MapView map) {
        hasRendered = false;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if (hasRendered == false) {
            canvas.drawImage(0, 0, image.getSubimage(offsetX, offsetZ, 128, 128));
            hasRendered = true;
        }
    }
}
