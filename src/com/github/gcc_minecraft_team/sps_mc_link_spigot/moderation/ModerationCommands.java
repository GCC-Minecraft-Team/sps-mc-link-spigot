package com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.haoshoku.nick.api.NickAPI;

public class ModerationCommands implements CommandExecutor {

    public static final String modPrefix = ChatColor.AQUA + "[SPSMC Mod System]: " + ChatColor.WHITE;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equalsIgnoreCase("banSPS")) {
            if (args.length != 2) {
                // No arguments or too many arguments for /mod banSPS
                sender.sendMessage(modPrefix + ChatColor.RED + "Usage: /" + label + " banSPS <player>");
                return true;
            } else {
                sender.sendMessage(modPrefix + ChatColor.YELLOW + "Banning Account: " + args[1]);
                if (DatabaseLink.banPlayer(args[1])) {
                    sender.sendMessage(modPrefix + ChatColor.GREEN + "[Account banned]");
                    return true;
                } else {
                    sender.sendMessage(modPrefix + ChatColor.RED + "[Account banning failed!]");
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("tpSPS")) {
            if (args.length != 2) {
                // No arguments or too many arguments for /mod banSPS
                sender.sendMessage(modPrefix + ChatColor.RED + "Usage: /" + label + " tpSPS <player>");
                return true;
            } else {
                sender.sendMessage(modPrefix + ChatColor.YELLOW + "Teleporting to: " + args[1]);
                ((Player) sender).teleport(NickAPI.getPlayerOfNickedName(args[1]).getLocation());
                return true;
            }
        } else if (args[0].equalsIgnoreCase("muteSPS")) {
            if (args.length != 2) {
                // No arguments or too many arguments for /mod muteSPS
                sender.sendMessage(modPrefix + ChatColor.RED + "Usage: /" + label + " muteSPS <player>");
                return true;
            } else {
                sender.sendMessage(modPrefix + ChatColor.YELLOW + "Muting account: " + args[1]);
                if (DatabaseLink.setMutePlayer(args[1], true)) {
                    sender.sendMessage(modPrefix + ChatColor.GREEN + "[Account muted]");
                    return true;
                } else {
                    sender.sendMessage(modPrefix + ChatColor.RED + "[Account muting failed!]");
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("unmuteSPS")) {
            if (args.length != 2) {
                // No arguments or too many arguments for /mod muteSPS
                sender.sendMessage(modPrefix + ChatColor.RED + "Usage: /" + label + " unmuteSPS <player>");
                return true;
            } else {
                sender.sendMessage(modPrefix + ChatColor.YELLOW + "Unmuting account: " + args[1]);
                if (DatabaseLink.setMutePlayer(args[1], false)) {
                    sender.sendMessage(modPrefix + ChatColor.GREEN + "[Account unmuted]");
                    return true;
                } else {
                    sender.sendMessage(modPrefix + ChatColor.RED + "[Account unmuting failed!]");
                    return true;
                }
            }
        } else {
            // args[0] is invalid
            return false;
        }
    }
}
