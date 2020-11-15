package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Starts ticking the compass at the bottom of the player's screen
 */
public class CompassThread implements Runnable {
    private Thread t;
    private Player player;
    private WorldGroup worldGroup;
    private boolean running;

    public CompassThread(Player player, WorldGroup worldGroup) {
        this.player = player;
        this.worldGroup = worldGroup;
        running = true;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "CompassThread");
            t.start();
        }
    }

    public void stop() {
        running = false;
        t.interrupt();
    }

    public void run() {
        while (running) {
            // compass
            String claimStatus = net.md_5.bungee.api.ChatColor.DARK_GREEN + "Wilderness";

            if (worldGroup == null || !worldGroup.isClaimable(player.getWorld())) {
                claimStatus = net.md_5.bungee.api.ChatColor.GRAY + "World not claimable!";
            } else {
                UUID chunkOwner = worldGroup.getChunkOwner(player.getLocation().getChunk());
                Team playerTeam = worldGroup.getPlayerTeam(player.getUniqueId());
                if (worldGroup.isInSpawn(player.getLocation()) && worldGroup.isClaimable(player.getWorld())) {
                    claimStatus = net.md_5.bungee.api.ChatColor.DARK_PURPLE + "[Spawn] Claiming Disabled";
                } else {
                    if (chunkOwner != null) {
                        if (playerTeam != null && playerTeam.getMembers().contains(chunkOwner)) {
                            claimStatus = net.md_5.bungee.api.ChatColor.AQUA + "[" + playerTeam.getName() + "] " + DatabaseLink.getSPSName(chunkOwner);
                        } else if (worldGroup.getPlayerTeam(chunkOwner) != null) {
                            claimStatus = net.md_5.bungee.api.ChatColor.RED + "[" + worldGroup.getPlayerTeam(chunkOwner).getName() + "] " + DatabaseLink.getSPSName(chunkOwner);
                        } else {
                            if (player.getUniqueId().equals(chunkOwner)) {
                                claimStatus = net.md_5.bungee.api.ChatColor.GREEN + DatabaseLink.getSPSName(chunkOwner);
                            } else {
                                claimStatus = net.md_5.bungee.api.ChatColor.RED + DatabaseLink.getSPSName(chunkOwner);
                            }
                        }
                    }
                }
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append("[" + SPSSpigot.getCardinalDirection(player) + "] " + claimStatus).create());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // don't print stack trace, this is most likely intentional
            }
        }
    }
}