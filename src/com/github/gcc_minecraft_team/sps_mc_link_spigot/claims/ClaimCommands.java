package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class ClaimCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        /**
         * Unclaims a 3x3 area of chunks (9 total if all points are availible)
         */
        if (command.getName().equalsIgnoreCase("unclaim")) {
            // unclaim 3x3
            Player player = (Player) sender;
            int claimedChunks = 0;
            ArrayList<Chunk> chunks = new ArrayList<Chunk>();

            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {

                    // get the surrounding chunks
                    Chunk chunk = SPSSpigot.server().getWorlds().get(0).getChunkAt(((int)player.getLocation().getX() / 16) + x, ((int)player.getLocation().getZ() / 16) + z);
                    UUID owner = SPSSpigot.claims().getChunkOwner(chunk);

                    if (!SPSSpigot.claims().checkInSpawn(player)) {
                        if (owner != null && owner.equals(player.getUniqueId())) {
                            chunks.add(chunk);
                            claimedChunks++;
                        }
                    }
                }
            }

            if (SPSSpigot.claims().unclaimChunkList(chunks)) {
                SPSSpigot.claims().saveCurrentClaims();
                sender.sendMessage(ChatColor.GREEN + "Unclaimed " + claimedChunks +" chunks! You are now at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                SPSSpigot.claims().updateClaimMap(player);
                return true;
            } else {
                SPSSpigot.claims().saveCurrentClaims();
                sender.sendMessage(ChatColor.RED + "Failed to unclaim chunks!");
                return true;
            }

        /***
         * Unclaims a 3x3 area of chunks (9 total if all points are availible)
         */
        } else if (command.getName().equalsIgnoreCase("claim")) {
            // /claim
            if (args.length == 0) {
                // No arguments
                // claim 3x3
                Player player = (Player) sender;
                int claimedChunks = 0;
                ArrayList<Chunk> chunks = new ArrayList<Chunk>();

                for (int z = -1; z <= 1; z++) {
                    for (int x = -1; x <= 1; x++) {

                        // get the surrounding chunks
                        Chunk chunk = SPSSpigot.server().getWorlds().get(0).getChunkAt(((int)player.getLocation().getX() / 16) + x, ((int)player.getLocation().getZ() / 16) + z);
                        UUID owner = SPSSpigot.claims().getChunkOwner(chunk);

                        if (!SPSSpigot.claims().checkInSpawn(player)) {
                            if (owner == null) {
                                if (SPSSpigot.claims().getMaxChunks(player) > SPSSpigot.claims().getChunkCount(player.getUniqueId())) {
                                    chunks.add(chunk);
                                    claimedChunks++;
                                }
                            }
                        }
                    }
                }

                if (SPSSpigot.claims().claimChunkList(player.getUniqueId(), chunks)) {
                    SPSSpigot.claims().saveCurrentClaims();
                    sender.sendMessage(ChatColor.GREEN + "Claimed " + claimedChunks +" chunks! You are now at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                    SPSSpigot.claims().updateClaimMap(player);
                    return true;
                } else {
                    SPSSpigot.claims().saveCurrentClaims();
                    sender.sendMessage(ChatColor.RED + "Failed to claim chunks!");
                    return true;
                }

            } else if (!(sender instanceof Player)) {
                // This isn't a player.
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            } else {
                /**
                 * Claims a single chunk
                 */
                Player player = (Player) sender;
                Chunk chunk = player.getLocation().getChunk();
                if (args[0].equalsIgnoreCase("chunk")) {
                    UUID owner = SPSSpigot.claims().getChunkOwner(chunk);

                    if (SPSSpigot.claims().checkInSpawn(player)) {
                        sender.sendMessage(ChatColor.RED + "You can't claim chunks in spawn! Try moving further away from the starting area!");
                        return true;
                    }

                    if (owner != null) {
                        // Chunk is already claimed.
                        sender.sendMessage(ChatColor.RED + "You cannot claim this chunk. It is already claimed by " + DatabaseLink.getSPSName(owner));
                        return true;
                    } else if (SPSSpigot.claims().getMaxChunks(player) <= SPSSpigot.claims().getChunkCount(player.getUniqueId())) {
                        // Player is at their claim limit.
                        sender.sendMessage(ChatColor.RED + "You cannot claim this chunk. You have already claimed " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks. Play more to earn more max chunks.");
                        return true;
                    } else {
                        // We have more claims and this chunk is unclaimed.
                        if (SPSSpigot.claims().claimChunk(player.getUniqueId(), chunk)) {
                            SPSSpigot.claims().saveCurrentClaims();
                            sender.sendMessage(ChatColor.GREEN + "Successfully claimed chunk! You are at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                            return true;
                        } else {
                            SPSSpigot.claims().saveCurrentClaims();
                            sender.sendMessage(ChatColor.RED + "Failed to claim chunk! Oh no, something went wrong. :(");
                            return true;
                        }
                    }
                /**
                 * Unclaims a single chunk
                 */
                } else if (args[0].equalsIgnoreCase("unchunk")) {
                    UUID owner = SPSSpigot.claims().getChunkOwner(chunk);
                    if (owner == null || !owner.equals(player.getUniqueId())) {
                        // Player doesn't own this chunk so can't unclaim it
                        sender.sendMessage(ChatColor.RED + "You cannot unclaim this chunk. You do not own it.");
                        return true;
                    } else {
                        // TODO: Decide if we need a confirmation system
                        if (SPSSpigot.claims().unclaimChunk(chunk)) {
                            SPSSpigot.claims().saveCurrentClaims();
                            sender.sendMessage(ChatColor.GREEN + "Successfully unclaimed chunk! You are now at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                            return true;
                        } else {
                            SPSSpigot.claims().saveCurrentClaims();
                            sender.sendMessage(ChatColor.RED + "Failed to unclaim chunk! Oh no, something went wrong. :(");
                            return true;
                        }
                    }
                } else if (args[0].equalsIgnoreCase("hide")) {
                    if (SPSSpigot.plugin().boards.get(player.getUniqueId()) != null && SPSSpigot.plugin().boards.get(player.getUniqueId()).isDeleted() == false) {
                        FastBoard board = SPSSpigot.plugin().boards.get(player.getUniqueId());
                        board.delete();
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't hide the claim map!");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("show")) {
                    SPSSpigot.showBoard(player);
                    return true;
                } else {
                    // args[0] is invalid
                    return false;
                }
            }
        } else {
            return false;
        }
    }
}
