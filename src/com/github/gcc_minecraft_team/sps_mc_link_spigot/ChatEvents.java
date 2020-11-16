package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.discord.DiscordWebhook;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions.Rank;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            if (SPSSpigot.plugin().mutedPlayers.contains(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Wooks  wike uwu've bewn chat banned! next time be a good wittwe minecwaft pwayew uwu!");
            } else {
                String message = e.getMessage(); // get the message

                e.setCancelled(true); // Cancel the event, so no message is sent (yet)
                String playerStr = "[" + DatabaseLink.getSPSName(e.getPlayer().getUniqueId()) + "]";
                String newMessageStr =
                        //"[" + ChatColor.ITALIC.toString() + ChatColor.GRAY + DatabaseLink.getSchoolTag(e.getPlayer().getUniqueId()) + ChatColor.RESET + "]"
                        //+ "[" + ChatColor.ITALIC.toString() + ChatColor.GRAY + DatabaseLink.getGradeTag(e.getPlayer().getUniqueId()) + ChatColor.RESET + "]" +
                        SPSSpigot.plugin().getRankTag(e.getPlayer().getUniqueId()) + ": " + ChatColor.WHITE
                        + message.replaceAll(e.getPlayer().getDisplayName(), ""); // format the message
                SPSSpigot.logger().log(Level.INFO, newMessageStr);

                BaseComponent playerName = new TextComponent(playerStr);
                playerName.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(DatabaseLink.getSchoolTag(e.getPlayer().getUniqueId()) + " - " + DatabaseLink.getGradeTag(e.getPlayer().getUniqueId()))));
                playerName.addExtra(new TextComponent(newMessageStr));

                for (Player on : SPSSpigot.server().getOnlinePlayers()) { // Loop through all online players
                    on.spigot().sendMessage(playerName); // Send the player the message
                }

                // send messages to a discord channel
                if (!PluginConfig.getChatWebhook().equals("")) {
                    DiscordWebhook webhook = new DiscordWebhook(PluginConfig.getChatWebhook());

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

        String deathMessage;
        if (event.getDeathMessage() != null) {
            deathMessage = event.getDeathMessage();
            for (Player player : SPSSpigot.server().getOnlinePlayers()) {
                deathMessage = deathMessage.replaceAll(player.getName(), "[" + NickAPI.getName(player) + "]");
            }
        } else {
            deathMessage = "[" + NickAPI.getName(event.getEntity()) + "] was killed by " + dc.toString();
        }

        event.setDeathMessage(ChatColor.DARK_PURPLE + deathMessage);

        Location deathLocation = event.getEntity().getLocation();
        event.getEntity().sendMessage(ChatColor.DARK_PURPLE + "Your death coordinates are: (" +
                (int)deathLocation.getX() + ", " +
                (int)deathLocation.getY() + ", " +
                (int)deathLocation.getZ() + ").");

        String deathCounter = ChatColor.RED + NickAPI.getName(event.getEntity()) + " has died " + ((Player)ent).getStatistic(Statistic.DEATHS) + " time(s) on the server.";
        Bukkit.broadcastMessage(deathCounter);
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
