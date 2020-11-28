package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AdminClaimCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use this command.");
            return true;
        } else {
            Player player = (Player) sender;
            if (args.length == 0) {
                // No arguments for /adminc
                return false;
            } else if (args[0].equalsIgnoreCase("override")) {
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
                if (worldGroup == null) {
                    sender.sendMessage(ChatColor.RED + "You must be in a world group to be able to override the claims in it.");
                    return true;
                } else if (worldGroup.hasOverride(player.getUniqueId())) {
                    worldGroup.setOverride(player.getUniqueId(), false);
                    sender.sendMessage(ChatColor.GREEN + "Disabled claim override.");
                    return true;
                } else {
                    worldGroup.setOverride(player.getUniqueId(), true);
                    sender.sendMessage(ChatColor.GREEN + "Enabled claim override.");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("unclaim")) {
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
                if (worldGroup == null) {
                    sender.sendMessage(ChatColor.RED + "You must be in a world group to be able to remove the claims in it.");
                    return true;
                } else {
                    UUID owner = worldGroup.getOwner(player.getLocation());
                    if (owner == null) {
                        sender.sendMessage(ChatColor.RED + "This chunk is already unclaimed.");
                        return true;
                    } else {
                        worldGroup.unclaimChunk(player.getLocation().getChunk(), owner);
                        sender.sendMessage(ChatColor.GREEN + "Successfully removed chunk from " + DatabaseLink.getSPSName(owner) + ".");
                        return true;
                    }
                }
            } else {
                // Invalid argument for /adminc
                return false;
            }
        }
    }
}
