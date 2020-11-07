package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.haoshoku.nick.api.NickAPI;

import java.util.UUID;

public class JoinEvent implements Listener {

    /**
     * Fired on player join.
     * @param event The {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (DatabaseLink.getIsBanned(event.getPlayer().getUniqueId())) {
            event.getPlayer().kickPlayer("The SPS account you linked has been banned! >:(");
        } else {
            SPSSpigot.perms().loadPermissions(event.getPlayer());
            event.getPlayer().sendMessage(PluginConfig.GetPluginMOTD());

            if (!DatabaseLink.isRegistered(event.getPlayer().getUniqueId())) {
                event.setJoinMessage("A player is joining the server!");

                // Create a token for the player
                String jwt = WebInterfaceLink.CreateJWT(event.getPlayer().getUniqueId().toString(), "SPS MC", "Register Token", 1000000);

                TextComponent message = new TextComponent(">> CLICK HERE <<");
                message.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginConfig.GetWebAppURL() + "/register?token=" + jwt));

                event.getPlayer().sendMessage(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + "Connect to your SPS profile to play!");
                event.getPlayer().spigot().sendMessage(message);

                Player player = event.getPlayer();
                Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), new Runnable() {
                    public void run() {
                        NickAPI.nick(player, "Player");
                        NickAPI.refreshPlayer(player);
                    }
                }, 20);

                player.sendTitle("Welcome to" + ChatColor.BLUE +" SPS MC!", "Please use the link in chat to link your account!", 10, 160, 10);

            } else {
                // claim map
                BukkitScheduler scheduler = SPSSpigot.server().getScheduler();
                scheduler.scheduleSyncRepeatingTask(SPSSpigot.plugin(), new Runnable() {
                    @Override
                    public void run() {
                        // compass
                        String claimStatus = net.md_5.bungee.api.ChatColor.DARK_GREEN + "Wilderness";
                        UUID chunkOwner = SPSSpigot.claims().getChunkOwner(event.getPlayer().getLocation().getChunk());
                        if (chunkOwner != null) {
                            if (SPSSpigot.claims().getPlayerTeam(event.getPlayer().getUniqueId()) != null && SPSSpigot.claims().getPlayerTeam(event.getPlayer().getUniqueId()).getMembers().contains(chunkOwner)) {
                                claimStatus = net.md_5.bungee.api.ChatColor.AQUA + "[" + SPSSpigot.claims().getPlayerTeam(event.getPlayer().getUniqueId()).getName() + "] " +  DatabaseLink.getSPSName(chunkOwner);
                            } else {
                                claimStatus = net.md_5.bungee.api.ChatColor.RED + DatabaseLink.getSPSName(chunkOwner);
                            }
                        }

                        SPSSpigot.claims().updateClaimMap(event.getPlayer());
                        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append("[" + SPSSpigot.getCardinalDirection(event.getPlayer()) + "] " + claimStatus).create());
                    }
                }, 0, 10);

                String userNoFormat =  DatabaseLink.getSPSName(event.getPlayer().getUniqueId());
                String newUser = ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + userNoFormat;
                event.setJoinMessage(newUser + " joined the server.");

                int maxLength = (userNoFormat.length() < 15) ? userNoFormat.length() : 15;

                Player player = event.getPlayer();
                SPSSpigot.claims().updateClaimMap(player);
                Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), new Runnable() {
                    public void run() {
                        NickAPI.nick(player, userNoFormat.substring(0, maxLength));
                        NickAPI.refreshPlayer(player);
                        player.setFoodLevel(player.getFoodLevel() - 1);
                    }
                }, 20);
            }
        }
    }

    /**
     * Fired on player leave
     * @param event
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage("");
        SPSSpigot.perms().removeAttachment(event.getPlayer());
    }

}
