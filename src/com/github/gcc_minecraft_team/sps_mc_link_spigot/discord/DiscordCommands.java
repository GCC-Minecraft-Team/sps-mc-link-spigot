package com.github.gcc_minecraft_team.sps_mc_link_spigot.discord;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.PluginConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

public class DiscordCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be ran as a player!");
            return true;
        }

        String senderName = DatabaseLink.getSPSName(((Player) sender).getUniqueId());

        if (command.getName().equalsIgnoreCase("report")) {
            if (args.length < 2){
                sender.sendMessage(ChatColor.RED + "Usage: /report <name> <message>");
                return true;
            } else if (DatabaseLink.getSPSPlayer(args[0]) == null) {
                // The player name is not recognized.
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' was not recognized.");
                return true;
            } else {
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                if (!PluginConfig.GetReportWebhook().equals("")) {
                    DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetReportWebhook());
                    webhook.setUsername("Reports");
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .addField("Reporter", senderName, true)
                            .addField("Reported", args[0], true)
                            .addField("Reason", message, false)
                    );

                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Player successfully reported!");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("modmail")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /modmail <message>");
                return true;
            } else {
                String message = String.join(" ", args);

                if (!PluginConfig.GetMessageWebhook().equals("")) {
                    DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetMessageWebhook());
                    webhook.setUsername("Modmail");
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .addField("Sender", senderName, false)
                            .addField("Message", message, false)
                    );

                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sender.sendMessage(ChatColor.GREEN + "Message successfully sent!");
                return true;
            }
        } else {
            // Invalid command
            return false;
        }
    }
}
