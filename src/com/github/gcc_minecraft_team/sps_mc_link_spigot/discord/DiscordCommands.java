package com.github.gcc_minecraft_team.sps_mc_link_spigot.discord;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.PluginConfig;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Arrays;

public class DiscordCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("This command can only be ran as a player!");
            return true;
        }

        String senderName = DatabaseLink.getSPSName(((Player) commandSender).getUniqueId());

        if(command.getName() == "report"){
            if(args.length < 2){
                commandSender.sendMessage("Usage: /report <name> <message>");
                return true;
            }

            if(DatabaseLink.getSPSPlayer(args[0]) == null){
                commandSender.sendMessage("Invalid Player");
                return true;
            }
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            if(PluginConfig.GetReportWebhook() != ""){
                DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetReportWebhook());
                webhook.setUsername("Reports");
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .addField("Reporter", senderName, true)
                        .addField("Reported", args[0], true)
                        .addField("Reason", message, true)
                );

                try {
                    webhook.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            commandSender.sendMessage("Player reported!");
            return true;
        } else if(command.getName() == "modmail"){
            if(args.length < 1){
                commandSender.sendMessage("Usage: /modmail <message>");
                return true;
            }

            String message = String.join(" ", args);

            if(PluginConfig.GetMessageWebhook() != ""){
                DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetMessageWebhook());
                webhook.setUsername("Modmail");
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .addField("Sender", senderName, true)
                        .addField("Message", message, true)
                );

                try {
                    webhook.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            commandSender.sendMessage("Message sent!");
            return true;
        }
        return false;
    }
}
