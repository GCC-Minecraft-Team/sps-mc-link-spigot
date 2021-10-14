package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.ClaimBoard;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
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
        // shutdown and remove the map thread
        ClaimBoard.removeBoard(event.getPlayer().getUniqueId());
        // remove the compass thread and shut it down
        if (SPSSpigot.plugin().compassThreads.get(event.getPlayer().getUniqueId()) != null) {
            SPSSpigot.plugin().compassThreads.get(event.getPlayer().getUniqueId()).stop();
            SPSSpigot.plugin().compassThreads.remove(event.getPlayer().getUniqueId());
        }

        event.setQuitMessage("");
    }
}
