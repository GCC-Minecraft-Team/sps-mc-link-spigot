package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.ClaimMap;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class LeaveEvent implements Listener {

    /**
     * Fired on player join.
     * @param event The {@link PlayerQuitEvent}.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // shutdown and remove the map thread
        Map<UUID, ClaimMap> m = SPSSpigot.plugin().getMaps();
        m.get(event.getPlayer().getUniqueId()).shutdown();
        m.remove(event.getPlayer().getUniqueId());

        event.setQuitMessage(ChatColor.BLUE.toString() + ChatColor.ITALIC.toString() + DatabaseLink.getSPSName(event.getPlayer().getUniqueId()) + " disconnected, bye!.");
    }
}
