package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClaimMapRenderer extends MapRenderer {

    private Location lastLoc;
    private MapView.Scale lastScale;

    @Override
    public void initialize(@NotNull MapView map) {
        map.setTrackingPosition(true);
        super.initialize(map);
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (MapRegistry.isClaimMap(player.getInventory().getItemInMainHand()) || MapRegistry.isClaimMap(player.getInventory().getItemInOffHand())) {
            World world = player.getWorld();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(world);
            Location loc = player.getLocation();

            map.setWorld(world);
            map.setCenterX(loc.getBlockX());
            map.setCenterZ(loc.getBlockZ());

            if (worldGroup != null) {
                if (lastLoc == null || Math.pow(loc.getX() - lastLoc.getX(), 2) + Math.pow(loc.getZ() - lastLoc.getZ(), 2) > 1 || lastScale == null || !lastScale.equals(map.getScale())) {
                    new Thread(new CanvasRenderer(canvas, map.getScale().ordinal(), world, worldGroup, loc, player.getUniqueId())).start();
                    lastLoc = loc;
                    lastScale = map.getScale();
                }
            }
        }
    }

    private static class CanvasRenderer implements Runnable {

        private MapCanvas canvas;
        private int scale;
        private World world;
        private WorldGroup worldGroup;
        private Location loc;
        private UUID player;

        public CanvasRenderer(MapCanvas canvas, int scale, World world, WorldGroup worldGroup, Location loc, UUID player) {
            this.canvas = canvas;
            this.scale = scale;
            this.world = world;
            this.worldGroup = worldGroup;
            this.loc = loc;
            this.player = player;
        }

        @Override
        public void run() {
            int magnitude = (int) Math.pow(2, scale);
            int cornerX = loc.getBlockX() - 64 * magnitude;
            int cornerZ = loc.getBlockZ() - 64 * magnitude;

            int cornerChunkX = Math.floorDiv(cornerX, 16);
            int cornerChunkZ = Math.floorDiv(cornerZ, 16);

            int mapChunkSize = (8*magnitude+1);
            UUID[] chunks = new UUID[mapChunkSize * mapChunkSize];
            for (int cz = 0; cz < mapChunkSize; cz++) {
                for (int cx = 0; cx < mapChunkSize; cx++) {
                    chunks[cx + cz * mapChunkSize] = worldGroup.getChunkOwner(world, cornerChunkX + cx, cornerChunkZ + cz);
                }
            }

            for (int x = 0; x < 128; x++) {
                for (int y = 0; y < 128; y++) {
                    int chunkX = Math.floorDiv(cornerX + magnitude * x, 16);
                    int chunkZ = Math.floorDiv(cornerZ + magnitude * y, 16);
                    UUID owner = chunks[chunkX - cornerChunkX + (chunkZ - cornerChunkZ) * mapChunkSize];
                    if (owner == null) {
                        canvas.setPixel(x, y, MapPalette.TRANSPARENT);
                    } else if (owner.equals(player)) {
                        canvas.setPixel(x, y, MapPalette.LIGHT_GREEN);
                    } else if (worldGroup.isOnSameTeam(player, owner)) {
                        canvas.setPixel(x, y, MapPalette.PALE_BLUE);
                    } else {
                        canvas.setPixel(x, y, MapPalette.RED);
                    }
                }
            }
        }
    }
}
