package com.github.gcc_minecraft_team.sps_mc_link_spigot.general;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GeneralCommands implements CommandExecutor {

    private static PermissionAttachment noMoveAttach;
    private static Map<UUID, Integer> teleportTaskIDMap = new HashMap<UUID, Integer>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String chatPrefix = ChatColor.AQUA + "[SPSMC Server]: " + ChatColor.WHITE;

        if(!(sender instanceof Player)){
            sender.sendMessage("This command can only be ran as a player!");
            return false;
        }

        // Teleports player back to spawn
        if(command.getName().equalsIgnoreCase("spawn")) {

            int teleportTaskID = -33;

            Player player = (Player) sender;
            player.sendMessage(chatPrefix + "[FREEZE] Teleporting you to spawn in 10 seconds, type " + ChatColor.RED + "/cancel" + ChatColor.WHITE +  " to cancel teleportation.");
            noMoveAttach = player.addAttachment(SPSSpigot.plugin());
            noMoveAttach.setPermission("spsmc.basic.move", false);

            // set a delay of 10 seconds
            teleportTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), new Runnable() {
                public void run() {
                    teleportTaskIDMap.remove(player.getUniqueId());
                    player.sendMessage(chatPrefix + "Teleporting...");
                    noMoveAttach.remove();
                    player.teleport(SPSSpigot.server().getWorlds().get(0).getSpawnLocation());
                }
            }, 20 * 10);

            // store task id in map for possible cancellation later
            teleportTaskIDMap.put(player.getUniqueId(), teleportTaskID);
            return true;

        // Cancel Teleportation
        } else if (command.getName().equalsIgnoreCase("cancel")) {
            if (noMoveAttach != null) {
                noMoveAttach.remove();
                if (!teleportTaskIDMap.isEmpty()) {
                    if (teleportTaskIDMap.get(((Player) sender).getUniqueId()) != -33) {
                        Bukkit.getScheduler().cancelTask(teleportTaskIDMap.get(((Player) sender).getUniqueId()));
                        teleportTaskIDMap.remove(((Player) sender).getUniqueId());
                        sender.sendMessage(chatPrefix + "" + ChatColor.RED + "Teleportation Cancelled!");
                    }
                }
            }

            return true;
        }

        return false;
    }
}
