package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import database.DatabaseLink;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {

    /**
     * Fired on player join.
     * @param event The {@link PlayerQuitEvent}.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.DARK_BLUE.toString() + ChatColor.DARK_BLUE.toString() + DatabaseLink.getSPSName(event.getPlayer().getUniqueId()) + " disconnected, bye!.");
    }
}
