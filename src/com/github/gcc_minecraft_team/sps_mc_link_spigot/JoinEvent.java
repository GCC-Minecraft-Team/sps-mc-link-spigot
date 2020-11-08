package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
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
        Player player = event.getPlayer();
        if (DatabaseLink.getIsBanned(player.getUniqueId())) {
            player.kickPlayer("The SPS account you linked has been banned! >:(");
        } else {
            SPSSpigot.perms().loadPermissions(player);
            player.sendMessage(PluginConfig.GetPluginMOTD());

            if (!DatabaseLink.isRegistered(player.getUniqueId())) {
                event.setJoinMessage("A player is joining the server!");

                // Create a token for the player
                String jwt = WebInterfaceLink.CreateJWT(player.getUniqueId().toString(), "SPS MC", "Register Token", 1000000);

                TextComponent message = new TextComponent(">> CLICK HERE <<");
                message.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginConfig.GetWebAppURL() + "/register?token=" + jwt));

                player.sendMessage(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + "Connect to your SPS profile to play!");
                player.spigot().sendMessage(message);

                Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> {
                    NickAPI.nick(player, "Player");
                    NickAPI.refreshPlayer(player);
                }, 20);

                player.sendTitle("Welcome to" + ChatColor.BLUE +" SPS MC!", "Please use the link in chat to link your account!", 10, 160, 10);

            } else {
                // claim map
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());

                BukkitScheduler scheduler = SPSSpigot.server().getScheduler();
                scheduler.scheduleSyncRepeatingTask(SPSSpigot.plugin(), () -> {
                    // compass
                    if (worldGroup == null)
                        return;
                    String claimStatus = net.md_5.bungee.api.ChatColor.DARK_GREEN + "Wilderness";
                    UUID chunkOwner = worldGroup.getChunkOwner(player.getLocation().getChunk());
                    Team playerTeam = worldGroup.getPlayerTeam(player.getUniqueId());
                    if (chunkOwner != null) {
                        if (playerTeam != null && playerTeam.getMembers().contains(chunkOwner)) {
                            claimStatus = net.md_5.bungee.api.ChatColor.AQUA + "[" + playerTeam.getName() + "] " +  DatabaseLink.getSPSName(chunkOwner);
                        } else {
                            claimStatus = net.md_5.bungee.api.ChatColor.RED + DatabaseLink.getSPSName(chunkOwner);
                        }
                    }

                    worldGroup.updateClaimMap(player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().append("[" + SPSSpigot.getCardinalDirection(player) + "] " + claimStatus).create());
                }, 0, 10);

                String userNoFormat =  DatabaseLink.getSPSName(player.getUniqueId());
                String newUser = ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + userNoFormat;
                event.setJoinMessage(newUser + " joined the server.");

                int maxLength = Math.min(userNoFormat.length(), 15);
                Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> {
                    NickAPI.nick(player, userNoFormat.substring(0, maxLength));
                    NickAPI.refreshPlayer(player);
                    player.setFoodLevel(player.getFoodLevel() - 1);
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
    }

    /**
     * Fired on player spawn in.
     * @param event The {@link PlayerRespawnEvent}.
     */
    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Location pLoc = event.getRespawnLocation();
        double zdist = pLoc.getZ() - event.getPlayer().getWorld().getSpawnLocation().getZ();
        double xdist = pLoc.getX() - event.getPlayer().getWorld().getSpawnLocation().getX();
        if (Math.abs(zdist) <= SPSSpigot.server().getSpawnRadius() && Math.abs(xdist) <= SPSSpigot.server().getSpawnRadius()) {
            // give starting boat
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.OAK_BOAT));
            SPSSpigot.showBoard(event.getPlayer());
        }
    }

}
