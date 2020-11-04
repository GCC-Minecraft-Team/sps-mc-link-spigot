package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordWebhook;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.api.NickAPI;

import javax.xml.crypto.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ChatEvents implements Listener {

    /**
     * Fires when someone send a message in chat
     * 
     * @param e The {@link AsyncPlayerChatEvent}.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (DatabaseLink.isRegistered(e.getPlayer().getUniqueId())) {
            String message = e.getMessage(); // get the message

            e.setCancelled(true); // Cancel the event, so no message is sent (yet)

            String newMessage = ChatColor.DARK_AQUA + "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId())
                    + "]" + SPSSpigot.GetRankTag(e.getPlayer()) + ": " + ChatColor.WHITE
                    + message.replaceAll(e.getPlayer().getDisplayName(), ""); // format the message

            for (Player on : SPSSpigot.server().getOnlinePlayers()) { // loop through all online players
                SPSSpigot.logger().log(Level.INFO, newMessage);
                on.sendMessage(newMessage); // send the player the message
            }

            // send messages to a discord channel
            if (!PluginConfig.GetChatWebhook().equals("")) {
                DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetChatWebhook());
                StringBuilder discordName = new StringBuilder();
                discordName.append(SPSSpigot.plugin().GetRankTagNoFormat(e.getPlayer()) + " ");
                discordName.append(NickAPI.getName(e.getPlayer()));

                webhook.setUsername(discordName.toString());
                String discordMsg = message.replaceAll(e.getPlayer().getDisplayName(), "");
                webhook.setContent(discordMsg);

                try {
                    webhook.execute();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Overrides certain bukkit commands with SPSMC versions
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().split(" ")[0].replace("/", "");

        if (command.equalsIgnoreCase("list")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GOLD + "[SPS MC] There are " + SPSSpigot.server().getOnlinePlayers().size()
                            + "/" + SPSSpigot.server().getMaxPlayers() + " online players!");

            StringBuilder str = new StringBuilder(ChatColor.BOLD + "Player List: \n\n");
            for (Rank rank : SPSSpigot.perms().getRanks()) {
                str.append(rank.getColor()).append(ChatColor.BOLD).append("~=[").append(rank.getName()).append("s]=~\n")
                        .append(ChatColor.RESET);
                for (UUID player : SPSSpigot.perms().getRankPlayers(rank)) {
                    if (SPSSpigot.server().getOfflinePlayer(player).isOnline())
                        str.append(DatabaseLink.getSPSName(player)).append(" ");
                }
                str.append("\n\n");
            }
            e.getPlayer().sendMessage(str.toString());

        } else if (command.equalsIgnoreCase("help")) {

            // TODO show some minecraft commands as well like "/list"
            // if the player does not have a rank, show the custom help message
            if (SPSSpigot.perms().getPlayerRanks(e.getPlayer().getUniqueId()).isEmpty()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.AQUA + "[SPS MC] Commands List:\n" + ChatColor.RESET);
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getName().equals(SPSSpigot.plugin().getName())) {
                        StringBuilder helpList = new StringBuilder();
                        List<Command> commandList = PluginCommandYamlParser.parse(plugin);
                        for (int i = 0; i < commandList.size(); i++) {
                            if (e.getPlayer().hasPermission(commandList.get(i).getPermission())) {
                                helpList.append("\n" + ChatColor.GOLD + commandList.get(i).getName() + ChatColor.WHITE
                                        + "  -  " + commandList.get(i).getDescription() + "\n");
                            }
                        }
                        helpList.append("\n");
                        e.getPlayer().sendMessage(helpList.toString());
                    }
                }
            }
        }
    }
}
