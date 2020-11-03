package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import javax.xml.crypto.Data;

public class ChatEvents implements Listener {

    /**
     * Fires when someone send a message in chat
     * @param e
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (DatabaseLink.isRegistered(e.getPlayer().getUniqueId())) {
            String message = e.getMessage(); //get the message

            e.setCancelled(true); //cancel the event, so no message is sent (yet)

            for (Player on : Bukkit.getOnlinePlayers()) { //loop threw all online players
                String newMessage = ChatColor.DARK_AQUA + "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId()) + "]: " + ChatColor.WHITE + message.replaceAll(e.getPlayer().getDisplayName(), ""); //format the message
                System.out.println(newMessage);
                on.sendMessage(newMessage); //send the player the message
            }
        }
    }

    /**
     * Overrides ceratin bukkit commands with SPSMC versions
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String command = e.getMessage().split(" ")[0].replace("/", "");

        if (command.equalsIgnoreCase("list")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.GOLD + "[SPS MC] There are " + Bukkit.getOnlinePlayers().stream().count() + " players out of " + Bukkit.getMaxPlayers() + " online!");

            // TODO: Check player ranks and list moderators, admins, etc.
            StringBuilder PlayerList = new StringBuilder();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerList.append(DatabaseLink.getSPSName(player.getUniqueId()) + ", ");
            }

            p.sendMessage( ChatColor.AQUA + "Members: " + PlayerList.substring(0, PlayerList.length() - 2));
        }
    }
}
