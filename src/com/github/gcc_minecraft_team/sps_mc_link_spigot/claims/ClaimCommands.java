package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use these commands.");
            return true;
        }
        Player player = (Player) sender;
        ClaimHandler worldGroup = SPSSpigot.claims(player.getWorld());
        if (worldGroup == null) {
            sender.sendMessage(ChatColor.RED + "This world is not in a world group, so claims cannot be made.");
            return true;
        }
        Chunk playerChunk = player.getLocation().getChunk();
        /*
         * Unclaims a 3x3 area of chunks (9 total if all points are availible)
         */
        if (command.getName().equalsIgnoreCase("unclaim")) {
            // unclaim 3x3
            Set<Chunk> chunks = new HashSet<>();

            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    // get the surrounding chunks
                    Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                    UUID owner = worldGroup.getChunkOwner(chunk);

                    if (!worldGroup.isInSpawn(player.getLocation())) {
                        if (owner != null && owner.equals(player.getUniqueId())) {
                            chunks.add(chunk);
                        }
                    }
                }
            }

            Set<Chunk> unclaimed = worldGroup.unclaimChunkSet(chunks);
            sender.sendMessage(ChatColor.GREEN + "Unclaimed " + unclaimed.size() + "/" + chunks.size() + " chunks! You are now at " + worldGroup.getChunkCount(player.getUniqueId()) + "/" + worldGroup.getMaxChunks(player) + " chunks you can currently claim.");
            worldGroup.updateClaimMap(player);
            return true;

        /*
         * Unclaims a 3x3 area of chunks (9 total if all points are availible)
         */
        } else if (command.getName().equalsIgnoreCase("claim")) {
            // /claim
            if (args.length == 0) {
                // No arguments
                // claim 3x3
                Set<Chunk> chunks = new HashSet<>();

                for (int z = -1; z <= 1; z++) {
                    for (int x = -1; x <= 1; x++) {

                        // get the surrounding chunks
                        Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                        UUID owner = worldGroup.getChunkOwner(chunk);

                        if (!worldGroup.isInSpawn(player.getLocation())) {
                            if (owner == null) {
                                if (worldGroup.getMaxChunks(player) > worldGroup.getChunkCount(player.getUniqueId())) {
                                    chunks.add(chunk);
                                }
                            }
                        }
                    }
                }

                Set<Chunk> claimed = worldGroup.claimChunkSet(player.getUniqueId(), chunks);
                sender.sendMessage(ChatColor.GREEN + "Unclaimed " + claimed.size() + "/" + chunks.size() + " chunks! You are now at " + worldGroup.getChunkCount(player.getUniqueId()) + "/" + worldGroup.getMaxChunks(player) + " chunks you can currently claim.");
                worldGroup.updateClaimMap(player);
                return true;
            } else {
                /*
                 * Claims a single chunk
                 */
                Chunk chunk = player.getLocation().getChunk();
                if (args[0].equalsIgnoreCase("chunk")) {
                    UUID owner = worldGroup.getChunkOwner(chunk);

                    if (worldGroup.isInSpawn(player.getLocation())) {
                        sender.sendMessage(ChatColor.RED + "You can't claim chunks in spawn! Try moving further away from the starting area!");
                        return true;
                    } else if (owner != null) {
                        // Chunk is already claimed.
                        sender.sendMessage(ChatColor.RED + "You cannot claim this chunk. It is already claimed by " + DatabaseLink.getSPSName(owner));
                        return true;
                    } else if (worldGroup.getMaxChunks(player) <= worldGroup.getChunkCount(player.getUniqueId())) {
                        // Player is at their claim limit.
                        sender.sendMessage(ChatColor.RED + "You cannot claim this chunk. You have already claimed " + worldGroup.getChunkCount(player.getUniqueId()) + "/" + worldGroup.getMaxChunks(player) + " chunks. Play more to earn more max chunks.");
                        return true;
                    } else {
                        // We have more claims and this chunk is unclaimed.
                        if (worldGroup.claimChunk(player.getUniqueId(), chunk)) {
                            sender.sendMessage(ChatColor.GREEN + "Successfully claimed chunk! You are at " + worldGroup.getChunkCount(player.getUniqueId()) + "/" + worldGroup.getMaxChunks(player) + " chunks you can currently claim.");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "Failed to claim chunk! Oh no, something went wrong. :(");
                            return true;
                        }
                    }
                /*
                 * Unclaims a single chunk
                 */
                } else if (args[0].equalsIgnoreCase("unchunk")) {
                    UUID owner = worldGroup.getChunkOwner(chunk);
                    if (owner == null || !owner.equals(player.getUniqueId())) {
                        // Player doesn't own this chunk so can't unclaim it
                        sender.sendMessage(ChatColor.RED + "You cannot unclaim this chunk. You do not own it.");
                        return true;
                    } else {
                        // TODO: Decide if we need a confirmation system
                        if (worldGroup.unclaimChunk(chunk)) {
                            sender.sendMessage(ChatColor.GREEN + "Successfully unclaimed chunk! You are now at " + worldGroup.getChunkCount(player.getUniqueId()) + "/" + worldGroup.getMaxChunks(player) + " chunks you can currently claim.");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "Failed to unclaim chunk! Oh no, something went wrong. :(");
                            return true;
                        }
                    }
                } else if (args[0].equalsIgnoreCase("hide")) {
                    if (SPSSpigot.plugin().boards.get(player.getUniqueId()) != null && !SPSSpigot.plugin().boards.get(player.getUniqueId()).isDeleted()) {
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
