package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimBoard {

    private static final Map<UUID, BoardUpdater> threads = new HashMap<>();
    private static final Map<UUID, FastBoard> boards = new HashMap<>();

    /**
     * Gets all the players who have boards.
     * @return An unmodifiable {@link Set} of the {@link UUID}s of players with boards.
     */
    @NotNull
    public static Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(boards.keySet());
    }

    /**
     * Gets whether a player has a board.
     * @param player The {@link UUID} of the player to check.
     * @return {@code true} if the player has a board that has not been deleted.
     */
    public static boolean hasBoard(@NotNull UUID player) {
        return boards.containsKey(player) && !boards.get(player).isDeleted();
    }

    /**
     * Creates a new board for a {@link Player} if they do not have one.
     * @param player The {@link Player} to create the board for.
     */
    public static void addBoard(@NotNull Player player) {
        if (!hasBoard(player.getUniqueId())) {
            FastBoard board = new FastBoard(player);
            board.updateTitle("[N]");
            boards.put(player.getUniqueId(), board);

            BoardUpdater bu = new BoardUpdater(player.getUniqueId());
            threads.put(player.getUniqueId(), bu);
            bu.start();
        }
    }

    /**
     * Removes a board for a {@link Player} if they have one.
     * @param player The {@link UUID} of the player to remove the board from.
     */
    public static void removeBoard(@NotNull UUID player) {
        if (threads.containsKey(player)) {
            threads.get(player).stopMap();
            threads.remove(player);
        }
        if (boards.containsKey(player)) {
            boards.get(player).delete();
            boards.remove(player);
        }
    }

    private static class BoardUpdater implements Runnable {

        private Thread t;
        private final UUID player;
        private boolean running;

        public BoardUpdater(@NotNull UUID player) {
            this.player = player;
            this.running = true;
        }

        /**
         * Stops this {@link BoardUpdater} updating.
         */
        public void stopMap() {
            this.running = false;
            t.interrupt();
        }

        /**
         * Creates and starts the {@link Thread} for this {@link BoardUpdater}.
         */
        public void start() {
            if (t == null) {
                t = new Thread(this, "ClaimMapThread");
                t.start();
            }
        }

        @Override
        public void run() {
            FastBoard board = boards.get(player);
            if (board == null)
                return;
            Player player = board.getPlayer();
            while (running) {
                Chunk playerChunk = player.getLocation().getChunk();
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
                if (worldGroup != null && !board.isDeleted()) {
                    // Map Strings
                    String[] rows = new String[7];

                    for (int z = -3; z <= 3; z++) {
                        StringBuilder bRow = new StringBuilder();
                        for (int x = -3; x <= 3; x++) {
                            // Get the surrounding chunks
                            Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                            UUID chunkOwner = worldGroup.getChunkOwner(chunk);
                            if (x == 0 && z == 0) {
                                // Player location.
                                ChatColor color;
                                if (worldGroup.isInSpawn(player.getLocation()))
                                    color = ChatColor.DARK_PURPLE; // In spawn
                                else if (chunkOwner == null)
                                    color = ChatColor.DARK_GRAY; // Unowned chunk outside spawn
                                else if (chunkOwner.equals(player.getUniqueId()))
                                    color = ChatColor.GREEN; // Player owns chunk
                                else if (worldGroup.isOnSameTeam(player.getUniqueId(), chunkOwner))
                                    color = ChatColor.AQUA; // Teammate owns chunk
                                else
                                    color = ChatColor.RED; // Other player owns chunk

                                String symbol;
                                float rotation = player.getLocation().getYaw();
                                if (45 <= rotation && rotation < 135)
                                    symbol = "Ⓦ";
                                else if (135 <= rotation && rotation < 225)
                                    symbol = "Ⓝ";
                                else if (225 <= rotation && rotation < 315)
                                    symbol = "Ⓔ";
                                else
                                    symbol = "Ⓢ";
                                bRow.append(color).append(symbol);
                            } else {
                                if (worldGroup.isClaimable(player.getWorld())) {
                                    if (worldGroup.isInSpawn(chunk.getBlock(0, 0, 0).getLocation())) {
                                        // In spawn
                                        bRow.append(ChatColor.DARK_PURPLE).append("Ⓢ");
                                    } else if (chunkOwner == null) {
                                        // Unowned, claimable chunk outside spawn
                                        bRow.append(ChatColor.DARK_GRAY).append("▒");
                                    } else if (chunkOwner.equals(player.getUniqueId())) {
                                        // Player owns chunk
                                        bRow.append(ChatColor.GREEN).append("█");
                                    } else if (worldGroup.isOnSameTeam(player.getUniqueId(), chunkOwner)) {
                                        // Teammate owns chunk
                                        bRow.append(ChatColor.AQUA).append("▒");
                                    } else {
                                        // Other player owns chunk
                                        bRow.append(ChatColor.RED).append("▒");
                                    }
                                } else {
                                    bRow.append(ChatColor.GRAY).append("✖");
                                }
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
                            rows[6],
                            "(" + (int) player.getLocation().getX() + ", " + (int) player.getLocation().getY() + ", " + (int) player.getLocation().getZ() + ")"
                    );
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // don't print stack trace, this is most likely intentional
                }
            }
        }

    }
}
