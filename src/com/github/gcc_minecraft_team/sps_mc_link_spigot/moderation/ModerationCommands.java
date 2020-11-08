package com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ModerationCommands implements CommandExecutor {

    public static final String modPrefix = ChatColor.AQUA + "[SPSMC Mod System]: " + ChatColor.WHITE;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equals("banSPS")) {
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
        } else {
            // args[0] is invalid
            return false;
        }
    }
}
