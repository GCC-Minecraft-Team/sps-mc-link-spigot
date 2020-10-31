package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!DatabaseLink.isRegistered(event.getPlayer().getUniqueId())) {
            event.setJoinMessage(event.getPlayer().getDisplayName() + " joined the server. Waiting for them to connect to their SPS profile.");
            event.getPlayer().sendMessage(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + "Connect to your SPS profile to play: " /* TODO: Link to webapp */);
            SPSSpigot.plugin().perms.loadPermissions(event.getPlayer(), false);
        } else {
            event.setJoinMessage(event.getPlayer().getDisplayName() + " (" + DatabaseLink.getSPSName(event.getPlayer().getUniqueId()) + ") joined the server.");
            SPSSpigot.plugin().perms.loadPermissions(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        SPSSpigot.plugin().perms.removeAttachment(event.getPlayer());
    }
}
