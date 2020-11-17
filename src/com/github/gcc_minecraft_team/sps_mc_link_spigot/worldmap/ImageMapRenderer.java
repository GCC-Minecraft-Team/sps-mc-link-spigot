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

public class ImageMapRenderer extends MapRenderer {

    private boolean hasRendered;
    private final int offsetX;
    private final int offsetZ;

    private BufferedImage image;

    public ImageMapRenderer(int xOffset, int zOffset, @NotNull String file) {
        this.offsetX = xOffset;
        this.offsetZ = zOffset;
        try {
            this.loadImage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageMapRenderer(@NotNull MapRegistry.ImageMap imageMap) {
        this(imageMap.xOffset, imageMap.yOffset, imageMap.file);
    }

    /**
     * Loads an image from the plugin data directory.
     * @param filename The name of the file to load.
     * @throws IOException If an error occurs during reading or when not able to create required {@link javax.imageio.stream.ImageInputStream}.
     */
    public void loadImage(String filename) throws IOException {
        image = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(), filename));
    }

    @Override
    public void initialize(@NotNull MapView map) {
        hasRendered = false;
    }

    @Override
    public void render(@NotNull MapView view, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (!hasRendered) {
            canvas.drawImage(0, 0, image.getSubimage(offsetX, offsetZ, 128, 128));
            hasRendered = true;
        }
    }
}
