package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ModerationCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String modPrefix = ChatColor.AQUA + "[SPSMC Mod System]: " + ChatColor.WHITE;

        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equals("banSPS")) {
            if (args.length == 1) {
                // No arguments for /perms rank
                sender.sendMessage(modPrefix + ChatColor.RED + "Usage: /" + label + " banSPS <sps username>");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage(modPrefix + ChatColor.YELLOW + "Banning Account: " + args[1]);
                if(DatabaseLink.banPlayer(args[1])) {
                    sender.sendMessage(modPrefix + ChatColor.GREEN + "[Account banned]");
                    return true;
                } else {
                    sender.sendMessage(modPrefix + ChatColor.RED + "[Account banning failed!]");
                    return false;
                }
            }
        }

        return false;
    }
}
