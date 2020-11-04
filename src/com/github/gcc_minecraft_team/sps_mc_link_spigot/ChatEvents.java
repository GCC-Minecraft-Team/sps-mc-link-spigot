package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.Rank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.logging.Level;

public class ChatEvents implements Listener {

    /**
     * Fires when someone send a message in chat
     * @param e The {@link AsyncPlayerChatEvent}.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (DatabaseLink.isRegistered(e.getPlayer().getUniqueId())) {
            String message = e.getMessage(); //get the message

            e.setCancelled(true); // Cancel the event, so no message is sent (yet)

            for (Player on : SPSSpigot.server().getOnlinePlayers()) { //loop through all online players
                String newMessage = ChatColor.DARK_AQUA + "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId()) + "]" + SPSSpigot.GetRankTag(e.getPlayer()) + ": " + ChatColor.WHITE + message.replaceAll(e.getPlayer().getDisplayName(), ""); //format the message
                SPSSpigot.logger().log(Level.INFO, newMessage);
                on.sendMessage(newMessage); //send the player the message
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
            e.getPlayer().sendMessage(ChatColor.GOLD + "[SPS MC] There are " + SPSSpigot.server().getOnlinePlayers().size() + "/" + SPSSpigot.server().getMaxPlayers() + " online players!");

            StringBuilder str = new StringBuilder(ChatColor.BOLD + "Player List: \n\n");
            for (Rank rank : SPSSpigot.perms().getRanks()) {
                str.append(rank.getColor()).append(ChatColor.BOLD).append("~=[").append(rank.getName()).append("s]=~\n").append(ChatColor.RESET);
                for (UUID player : SPSSpigot.perms().getRankPlayers(rank)) {
                    if (SPSSpigot.server().getOfflinePlayer(player).isOnline())
                        str.append(DatabaseLink.getSPSName(player)).append(" ");
                }
                str.append("\n\n");
            }
            e.getPlayer().sendMessage(str.toString());
        }
    }
}
