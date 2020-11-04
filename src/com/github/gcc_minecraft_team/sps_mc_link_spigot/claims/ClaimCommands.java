package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClaimCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /claim
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (!(sender instanceof Player)) {
            // This isn't a player.
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        } else {
            Player player = (Player) sender;
            Chunk chunk = player.getLocation().getChunk();
            if (args[0].equalsIgnoreCase("chunk")) {
                UUID owner = SPSSpigot.claims().getChunkOwner(chunk);
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
                        sender.sendMessage(ChatColor.GREEN + "Successfully claimed chunk! You are at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to claim chunk! Oh no, something went wrong. :(");
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("unchunk")) {
                UUID owner = SPSSpigot.claims().getChunkOwner(chunk);
                if (owner == null || !owner.equals(player.getUniqueId())) {
                    // Player doesn't own this chunk so can't unclaim it
                    sender.sendMessage(ChatColor.RED + "You cannot unclaim this chunk. You do not own it.");
                    return true;
                } else {
                    // TODO: Decide if we need a confirmation system
                    if (SPSSpigot.claims().unclaimChunk(chunk)) {
                        sender.sendMessage(ChatColor.GREEN + "Successfully unclaimed chunk! You are now at " + SPSSpigot.claims().getChunkCount(player.getUniqueId()) + "/" + SPSSpigot.claims().getMaxChunks(player) + " chunks you can currently claim.");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to unclaim chunk! Oh no, something went wrong. :(");
                        return true;
                    }
                }
            } else {
                // args[0] is invalid
                return false;
            }
        }
    }
}
