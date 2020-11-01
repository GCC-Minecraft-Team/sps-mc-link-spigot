package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvents implements Listener {

    /*
    Replace the player's name with our SPS name
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        String message = e.getMessage(); //get the message

        e.setCancelled(true); //cancel the event, so no message is sent (yet)

        for(Player on : Bukkit.getOnlinePlayers()){ //loop threw all online players
            String newMessage = ChatColor.DARK_AQUA + "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId()) + "]: " + ChatColor.WHITE + message.replaceAll(e.getPlayer().getDisplayName(), ""); //format the message
            System.out.println(newMessage);
            on.sendMessage(newMessage); //send the player the message
        }
    }
}
