package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimMap extends Thread {

    private Thread t;
    private Player player;
    private boolean shutdown;

    public ClaimMap(Player player) {
        this.player = player;
        shutdown = false;
    }

    public void start() {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }

    @Override
    public void run() {
        while (!shutdown) {
            Chunk playerChunk = player.getLocation().getChunk();
            FastBoard board = SPSSpigot.plugin().boards.get(player.getUniqueId());
            WorldGroup wg = SPSSpigot.getWorldGroup(player.getWorld());
            if (board != null && !board.isDeleted()) {
                // map
                String[] rows = new String[7];

                for (int z = -3; z <= 3; z++) {
                    StringBuilder bRow = new StringBuilder();
                    for (int x = -3; x <= 3; x++) {
                        // get the surrounding chunks

                        if (!wg.isEntityInSpawn(player)) {
                            Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                            UUID chunkOwner = wg.getChunkOwner(chunk);
                            if (x == 0 && z == 0) {
                                bRow.append(ChatColor.BLUE).append("Ⓟ");
                            } else {
                                if (chunkOwner == null) {
                                    // Unowned / in spawn
                                    bRow.append(ChatColor.GRAY).append("▒");
                                } else if (chunkOwner.equals(player.getUniqueId())) {
                                    // Player owns chunk
                                    bRow.append(ChatColor.GREEN).append("█");
                                } else if (wg.isOnSameTeam(player.getUniqueId(), chunkOwner)) {
                                    // Teammate owns chunk
                                    bRow.append(ChatColor.AQUA).append("▒");
                                } else {
                                    // Other player owns chunk
                                    bRow.append(ChatColor.RED).append("▒");
                                }
                            }
                        } else {
                            // spawn
                            bRow.append(ChatColor.DARK_PURPLE).append("Ⓢ");
                        }
                    }
                    rows[z + 3] = bRow.toString();
                }

                // update the board
                board.updateLines(
                        rows[0],
                        rows[1],
                        rows[2],
                        rows[3],
                        rows[4],
                        rows[5],
                        rows[6]
                );
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        Thread.currentThread().interrupt();
        return;
    }

    public void startup() {
        shutdown = false;
    }

    public void shutdown() {
        shutdown = true;
    }
}
