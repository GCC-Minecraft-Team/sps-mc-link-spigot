package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.ClaimBoard;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.nametagedit.plugin.NametagEdit;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.haoshoku.nick.api.NickAPI;

public class JoinEvent implements Listener {

    /**
     * Fired on player join.
     * @param event The {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (DatabaseLink.isBanned(player.getUniqueId())) {
            player.kickPlayer("The SPS account you linked has been banned! >:(");
        } else {
            if (!SPSSpigot.plugin().mutedPlayers.contains(player.getUniqueId())) {
                if (DatabaseLink.getIsMuted(player.getUniqueId())) {
                    SPSSpigot.plugin().mutedPlayers.add(player.getUniqueId());
                }
            }

            SPSSpigot.perms().loadPermissions(player);
            player.sendMessage(PluginConfig.getPluginMOTD());

            if (!DatabaseLink.isRegistered(player.getUniqueId())) {
                player.kickPlayer("Something went wrong verifying your SPS account, please contact an admin!");
            } else {
                ClaimBoard.addBoard(player);

                // claim map
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
                if (worldGroup != null) {
                    CompassThread compass = new CompassThread(player, worldGroup);
                    SPSSpigot.plugin().compassThreads.put(player.getUniqueId(), compass);
                    compass.start();
                }

                String userNoFormat =  DatabaseLink.getSPSName(player.getUniqueId());
                String newUser = ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + userNoFormat;
                String school = DatabaseLink.getSchoolTag(player.getUniqueId());
                String grade = DatabaseLink.getGradeTag(player.getUniqueId());
                event.setJoinMessage(newUser + " (" + school + ", " + grade + ")" + " entered the world!");

                int maxLength = Math.min(userNoFormat.length(), 15);
                Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> {
                    NickAPI.nick(player, userNoFormat);
                    NickAPI.refreshPlayer(player);
                }, 20);
            }
        }
    }

    /**
     * Fired on player leave.
     * @param event The {@link PlayerQuitEvent}.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage("");
        SPSSpigot.perms().removeAttachment(event.getPlayer());
        for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
            worldGroup.setOverride(event.getPlayer().getUniqueId(), false);
    }

    /**
     * Fired on player spawn in.
     * @param event The {@link PlayerRespawnEvent}.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location pLoc = event.getRespawnLocation();
        double zdist = pLoc.getZ() - event.getPlayer().getWorld().getSpawnLocation().getZ();
        double xdist = pLoc.getX() - event.getPlayer().getWorld().getSpawnLocation().getX();
        if (Math.abs(zdist) <= SPSSpigot.server().getSpawnRadius() && Math.abs(xdist) <= SPSSpigot.server().getSpawnRadius()) {
            SPSSpigot.plugin().giveStartingItems(event.getPlayer());
        }
    }
}
