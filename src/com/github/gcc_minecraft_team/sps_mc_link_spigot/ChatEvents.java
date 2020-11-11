package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordWebhook;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.Rank;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import xyz.haoshoku.nick.api.NickAPI;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ChatEvents implements Listener {

    /**
     * Fires when someone send a message in chat.
     * @param e The {@link AsyncPlayerChatEvent}.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (DatabaseLink.isRegistered(e.getPlayer().getUniqueId())) {
            String message = e.getMessage(); // get the message

            e.setCancelled(true); // Cancel the event, so no message is sent (yet)

            String newMessage = ChatColor.DARK_AQUA + "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId())
                    + "]" + SPSSpigot.plugin().getRankTag(e.getPlayer().getUniqueId()) + ": " + ChatColor.WHITE
                    + message.replaceAll(e.getPlayer().getDisplayName(), ""); // format the message

            for (Player on : SPSSpigot.server().getOnlinePlayers()) { // loop through all online players
                //SPSSpigot.logger().log(Level.INFO, newMessage);
                on.sendMessage(newMessage); // send the player the message
            }

            // send messages to a discord channel
            if (!PluginConfig.GetChatWebhook().equals("")) {
                DiscordWebhook webhook = new DiscordWebhook(PluginConfig.GetChatWebhook());

                String discordName = SPSSpigot.plugin().getRankTagNoFormat(e.getPlayer().getUniqueId()) + " " +
                        NickAPI.getName(e.getPlayer());
                webhook.setUsername(discordName);
                String discordMsg = message.replaceAll(e.getPlayer().getDisplayName(), "");
                webhook.setContent(discordMsg);

                try {
                    webhook.execute();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Overrides death messages.
     * @param event The {@link PlayerDeathEvent}.
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Entity ent = event.getEntity();
        EntityDamageEvent ede = ent.getLastDamageCause();
        EntityDamageEvent.DamageCause dc = ede.getCause();

        if (ent.getLastDamageCause() instanceof Player) {
            event.setDeathMessage(ChatColor.DARK_PURPLE + "[" + NickAPI.getName(event.getEntity()) + "] Was killed by " + NickAPI.getName((Player) ent.getLastDamageCause()));
        } else {
            event.setDeathMessage(ChatColor.DARK_PURPLE + "[" + NickAPI.getName(event.getEntity()) + "] Was killed by " + dc.toString());
        }

        Location deathLocation = event.getEntity().getLocation();
        event.getEntity().sendMessage(ChatColor.DARK_PURPLE + "Your death coordinates are: " +
                (int)deathLocation.getX() + ", " +
                (int)deathLocation.getY() + ", " +
                (int)deathLocation.getZ());
    }

    /**
     * Overrides certain bukkit commands with SPSMC versions.
     * @param e The {@link PlayerCommandPreprocessEvent}.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().split(" ")[0].replace("/", "");

        if (command.equalsIgnoreCase("list")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GOLD + "[SPS MC] There are " + SPSSpigot.server().getOnlinePlayers().size()
                            + "/" + SPSSpigot.server().getMaxPlayers() + " online players!");

            StringBuilder str = new StringBuilder(ChatColor.BOLD + "Player List: \n\n");
            for (Rank rank : SPSSpigot.perms().getRanks()) {
                str.append(rank.getColor()).append(ChatColor.BOLD).append("~=[").append(rank.getName()).append("s]=~\n")
                        .append(ChatColor.RESET);
                for (UUID player : SPSSpigot.perms().getRankPlayers(rank)) {
                    if (SPSSpigot.server().getOfflinePlayer(player).isOnline())
                        str.append(DatabaseLink.getSPSName(player)).append(" ");
                }
                str.append("\n\n");
            }
            e.getPlayer().sendMessage(str.toString());

        } else if (command.equalsIgnoreCase("help")) {
            // TODO show some minecraft commands as well like "/list"
            // if the player does not have a rank, show the custom help message
            if (SPSSpigot.perms().getPlayerRanks(e.getPlayer().getUniqueId()).isEmpty()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.AQUA + "[SPS MC] Commands List:\n" + ChatColor.RESET);
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getName().equals(SPSSpigot.plugin().getName())) {
                        StringBuilder helpList = new StringBuilder();
                        List<Command> commandList = PluginCommandYamlParser.parse(plugin);
                        for (Command value : commandList) {
                            if (value.getPermission() == null || e.getPlayer().hasPermission(value.getPermission())) {
                                helpList.append("\n" + ChatColor.GOLD).append(value.getName()).append(ChatColor.WHITE).append("  -  ").append(value.getDescription()).append("\n");
                            }
                        }
                        e.getPlayer().sendMessage(helpList.toString());
                    }
                }
            }
        }
    }
}
