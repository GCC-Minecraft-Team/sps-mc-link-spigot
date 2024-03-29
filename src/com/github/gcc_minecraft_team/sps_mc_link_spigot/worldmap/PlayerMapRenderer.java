package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CompassThread;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

class RenderPlayerClaims implements Runnable {
    private Thread t;
    private WorldGroup worldGroup;
    private Map<Team, ArrayList<Chunk>> claims;
    volatile boolean running = false;

    public RenderPlayerClaims(WorldGroup worldGroup) {
        this.worldGroup = worldGroup;
        claims = new HashMap<Team, ArrayList<Chunk>>();
        running = true;
    }

    public void start() {
        if (t == null || !t.isAlive()) {
            t = new Thread(this, "RenderPlayerClaims");
            t.start();
        }
    }

    public void stop() {
        running = false;
        t.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            Set<Team> teams = DatabaseLink.getTeams(worldGroup);
            Map<UUID, Set<Chunk>> playerClaims = DatabaseLink.getClaims(worldGroup);

            for (Map.Entry<UUID,Set<Chunk>> player : playerClaims.entrySet()) {
                Team playerTeam = null;
                for (Team team : teams) {
                    if (team.getMembers().contains(player.getKey())) {
                        playerTeam = team;
                    }
                }

                if (playerTeam != null) {
                    // if we have an existing team in claims, add this player's claims
                    // to the team's list of chunks claimed
                    if (claims != null && claims.containsKey(playerTeam)) {
                        ArrayList<Chunk> teamClaims = new ArrayList<Chunk>();
                        teamClaims.addAll(player.getValue());
                        teamClaims.addAll(claims.get(playerTeam));

                        claims.put(playerTeam, teamClaims);
                    } else {
                        ArrayList<Chunk> teamClaims = new ArrayList<Chunk>();
                        teamClaims.addAll(player.getValue());
                        claims.put(playerTeam,teamClaims);
                    }
                }
            }

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                // don't print stack trace, this is most likely intentional
            }
        }
    }

    public Map<Team, ArrayList<Chunk>> getClaims() {
        return claims;
    }
}

public class PlayerMapRenderer extends MapRenderer {

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
    private final int offsetX;
    private final int offsetZ;

    private Map<Team, ArrayList<Chunk>> claims;

    private BufferedImage background;
    private BufferedImage frame;
    private RenderPlayerClaims renderPlayerClaims;

    public PlayerMapRenderer(int xOffset, int zOffset) {
        this.offsetX = xOffset;
        this.offsetZ = zOffset;
        claims = new HashMap<Team, ArrayList<Chunk>>();
    }

    public PlayerMapRenderer(@NotNull MapRegistry.PlayerMap playerMap) {
        this(playerMap.xOffset, playerMap.zOffset);
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
            BufferedImage image = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(),"frame.png"));
            frame = image.getSubimage((offsetX / 16 + image.getWidth()/2)/128 * 128, (offsetZ / 16 + image.getHeight()/2)/128 * 128, 128, 128);
        } catch (IOException exception) {
            SPSSpigot.logger().log(Level.WARNING, "Failed to load frame image file for PlayerMap.");
        } catch (RasterFormatException e) {
            SPSSpigot.logger().log(Level.WARNING, "Failed to render frame for PlayerMap for offset (X: " + offsetX + ", Z: " + offsetZ + ") due to invalid image size.");
        }

         // generate images
        try {
            generateWorldBackground();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // this ended up being too slow
        // TODO: Figure out how to make this faster
        //enderPlayerClaims = new RenderPlayerClaims(SPSSpigot.getWorldGroup(SPSSpigot.server().getWorlds().get(0)));
        //renderPlayerClaims.start();

        // load generated images
        try {
            background = ImageIO.read(new File(SPSSpigot.plugin().getDataFolder(),"worldCache" + offsetZ + "," +  offsetX +".png"));
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

            //claims = renderPlayerClaims.getClaims();

            // load background cache
            canvas.drawImage(0, 0, background);

            // loops through map pixels
            // TODO: cache the world data
            for (int x = 0; x < 128; x++) {
                for (int z = 0; z < 128; z++) {
                    Location spawn = world.getSpawnLocation();

                    // player detection
                    for (Player p : onlinePlayers) {
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

                    // unfortunately this is too slow
                    /*
                    if (claims != null) {
                        int seed = 0;
                        for (Map.Entry<Team, ArrayList<Chunk>> claimSet : claims.entrySet()) {
                            Random rand = new Random();
                            rand.setSeed(seed);
                            int teamColor = rand.nextInt(255 - 0) + 0;
                            for (Chunk chunk : claimSet.getValue()) {
                                try {
                                    if (chunk.getBlock(0,0,0).getLocation().getX() < (((x + 1) * 16) + offsetX) + 32 && chunk.getBlock(0,0,0).getLocation().getX() >= (((x + 1) * 16) + offsetX) - 16) {
                                        if (chunk.getBlock(0,0,0).getLocation().getZ() < (((z + 1) * 16) + offsetZ) + 32 && chunk.getBlock(0,0,0).getLocation().getZ() >= (((z + 1) * 16) + offsetZ) - 16) {
                                            canvas.setPixel(x, z, MapPalette.matchColor(teamColor, 0, teamColor));
                                        }
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                            seed++;
                        }
                    }*/
                }
            }
        }
        hasRendered = true;
    }

    /**
     * Generates the image used as the background of the map
     */
    private void generateWorldBackground() throws IOException {
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
                } else if (biome == Biome.MUSHROOM_FIELDS || biome == Biome.MUSHROOM_FIELD_SHORE) {
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
        try {
            g2d.drawImage(frame, 0, 0, null);
        } catch (Exception e) {
            // we just don't get a frame.
        }

        // write background to file
        File file = new File(SPSSpigot.plugin().getDataFolder(),"worldCache" + offsetZ + "," + offsetX +".png");
        ImageIO.write(img, "png", file);
    }
}
