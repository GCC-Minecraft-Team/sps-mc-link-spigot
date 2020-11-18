package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MapCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        } else {
            Player player = (Player) sender;
            if (args.length == 0) {
                // No arguments for /maps
                return false;
            } else if (args[0].equals("image")) {
                if (!sender.hasPermission("spsmc.map.image")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to create image maps.");
                    return true;
                } else if (args.length == 2) {
                    if (!new File(SPSSpigot.plugin().getDataFolder(), args[1]).exists()) {
                        sender.sendMessage(ChatColor.RED + "Could not access file '" + args[1] + "'.");
                        return true;
                    } else {
                        MapRegistry.generateImageMap(player, args[1]);
                        return true;
                    }
                } else {
                    // Invalid number of arguments for /maps image
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " image <file>");
                    return true;
                }
            } else if (args[0].equals("players")) {
                if (!sender.hasPermission("spsmc.map.players")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to create players maps.");
                    return true;
                } else if (args.length == 3) {
                    int sizeX;
                    int sizeZ;
                    try {
                        sizeX = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "X argument '" + args[1] + "' was not recognized as a valid integer.");
                        return true;
                    }
                    try {
                        sizeZ = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Z argument '" + args[1] + "' was not recognized as a valid integer.");
                        return true;
                    }
                    MapRegistry.generatePlayerMap(player, sizeX, sizeZ);
                    return true;
                } else {
                    // Invalid number of arguments for /maps players
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " players <# of maps x-wise> <# of maps z-wise>");
                    return true;
                }
            } else if (args[0].equals("claims")) {
                if (!sender.hasPermission("spsmc.map.claims")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to create claims maps.");
                    return true;
                } else {
                    MapRegistry.generateClaimMap(player);
                    return true;
                }
            } else {
                // Invalid argument for /maps
                return false;
            }
        }
    }
}
