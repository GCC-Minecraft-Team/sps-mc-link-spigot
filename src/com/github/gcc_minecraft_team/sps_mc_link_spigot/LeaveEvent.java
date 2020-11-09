package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        FastBoard board = SPSSpigot.plugin().boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }

        event.setQuitMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC.toString() + DatabaseLink.getSPSName(event.getPlayer().getUniqueId()) + " disconnected, bye!.");
    }
}
