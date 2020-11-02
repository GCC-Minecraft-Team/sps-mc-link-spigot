package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.haoshoku.nick.api.NickAPI;

public class JoinEvent implements Listener {

    /**
     * Fired on player join
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (DatabaseLink.getIsBanned(event.getPlayer().getUniqueId())) {
            event.getPlayer().kickPlayer("The SPS account you linked has been banned! >:(");
        } else {
            SPSSpigot.plugin().perms.loadPermissions(event.getPlayer());
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
                //NickAPI.setSkin( player, player.getName() );
                //NickAPI.setUniqueId( player, player.getName() );
                NickAPI.nick(player, "Player");
                NickAPI.refreshPlayer(player);

            } else {
                String userNoFormat = DatabaseLink.getSPSName(event.getPlayer().getUniqueId());
                String newUser = ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + userNoFormat;
                event.setJoinMessage(newUser + " joined the server.");

                int maxLength = (userNoFormat.length() < 15) ? userNoFormat.length() : 15;

                Player player = event.getPlayer();
                //NickAPI.setSkin( player, player.getName() );
                //NickAPI.setUniqueId( player, player.getName() );
                NickAPI.nick(player, userNoFormat.substring(0, maxLength));
                NickAPI.refreshPlayer(player);
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
        SPSSpigot.plugin().perms.removeAttachment(event.getPlayer());
    }

}
