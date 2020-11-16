package com.github.gcc_minecraft_team.sps_mc_link_spigot.general;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.Team;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

public class GeneralCommands implements CommandExecutor {

    private static PermissionAttachment noMoveAttach;
    private static Map<UUID, Integer> teleportTaskIDMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String chatPrefix = ChatColor.AQUA + "[SPSMC Server]: " + ChatColor.WHITE;

        if (!(sender instanceof Player)){
            sender.sendMessage("This command can only be ran as a player!");
            return false;
        }

        // Teleports player back to spawn
        if (command.getName().equalsIgnoreCase("spawn")) {

            int teleportTaskID = -33;

            Player player = (Player) sender;
            player.sendMessage(chatPrefix + "[FREEZE] Teleporting you to spawn in 10 seconds, type " + ChatColor.RED + "/cancel" + ChatColor.WHITE +  " to cancel teleportation.");
            noMoveAttach = player.addAttachment(SPSSpigot.plugin());
            noMoveAttach.setPermission("spsmc.basic.move", false);

            // set a delay of 10 seconds
            teleportTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> {
                teleportTaskIDMap.remove(player.getUniqueId());
                player.sendMessage(chatPrefix + "Teleporting...");
                noMoveAttach.remove();
                player.teleport(SPSSpigot.server().getWorlds().get(0).getSpawnLocation());
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
        } else if (command.getName().equalsIgnoreCase("stats")) {
            StringBuilder statsStr = new StringBuilder(ChatColor.BOLD + "╔══[PLAYER STATS]══╗" + ChatColor.RESET);
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(((Player) sender).getWorld());

            if (args.length == 0) {
                // Getting stats and player names from the DB takes some time so we'll put it on a separate thread.
                (new Thread() {
                    public void run() {
                        int playerRank = 1;
                        List<OfflinePlayer> sortedPlayers = Arrays.stream(SPSSpigot.server().getOfflinePlayers()).sorted(Comparator.comparingInt(sp -> -1 * sp.getStatistic(Statistic.PLAY_ONE_MINUTE))).limit(4).collect(Collectors.toList());
                        for (OfflinePlayer p : sortedPlayers) {
                            if (p != null) {
                                String name = ChatColor.GOLD + "" + ChatColor.BOLD + DatabaseLink.getSPSName(p.getUniqueId()) + ChatColor.RESET;
                                String school = DatabaseLink.getSchoolTag(p.getUniqueId());
                                String grade = DatabaseLink.getGradeTag(p.getUniqueId());

                                // team display
                                Team t = worldGroup.getPlayerTeam(p.getUniqueId());
                                String team = "";
                                if (t != null) {
                                    team = " - " + ChatColor.AQUA + t.getName() + ChatColor.WHITE;
                                }

                                statsStr.append("\n" + ChatColor.WHITE + playerRank + ": " + name + " (" + school + ", " + grade + ")" + team);
                                statsStr.append(ChatColor.GRAY + "\n---| Playtime: " + ((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) / 60) / 60 + "h");
                                statsStr.append(" | K/D: " + p.getStatistic(Statistic.PLAYER_KILLS) + "/" + p.getStatistic(Statistic.DEATHS));
                                statsStr.append(" | Claims: " + worldGroup.getChunkCount(p.getUniqueId()) + "/" + worldGroup.getMaxChunks(p));
                                statsStr.append("\n");
                                playerRank++;
                            }
                        }
                        statsStr.append(ChatColor.WHITE + "" + ChatColor.BOLD + "╚══[PLAYER STATS]══╝" + ChatColor.RESET);

                        sender.sendMessage(statsStr.toString());

                        this.interrupt();
                        return;
                    }
                }).start();
            } else if (args.length == 1 && args[0] != null) {
                OfflinePlayer p = (OfflinePlayer) DatabaseLink.getSPSPlayer(args[0]);
                if (p != null) {
                    String name = ChatColor.GOLD + "" + args[0] + ChatColor.RESET;
                    String school = DatabaseLink.getSchoolTag(p.getUniqueId());
                    String grade = DatabaseLink.getGradeTag(p.getUniqueId());

                    // basic stats
                    statsStr.append("\n  " + ChatColor.WHITE + name + " (" + school + ", " + grade + ")" + ChatColor.GRAY);
                    statsStr.append("\n   Ranks: " + SPSSpigot.plugin().getRankTag(p.getUniqueId()));
                    statsStr.append("\n   Playtime: " + ChatColor.LIGHT_PURPLE + ((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) / 60) / 60 + "h" + ChatColor.GRAY);
                    statsStr.append("\n   Kills/Deaths: " + p.getStatistic(Statistic.PLAYER_KILLS) + "/" + p.getStatistic(Statistic.DEATHS));
                    statsStr.append("\n   Claims: " + ChatColor.GREEN + worldGroup.getChunkCount(p.getUniqueId()) + "/" + worldGroup.getMaxChunks(p) + ChatColor.GRAY);

                    // team display
                    Team t = worldGroup.getPlayerTeam(p.getUniqueId());
                    if (t != null) {
                        statsStr.append("\n   Team: " + ChatColor.AQUA + t.getName() + ChatColor.GRAY + " (Owned by: " + DatabaseLink.getSPSName(t.getLeader()) + ")");
                    } else {
                        statsStr.append("\n   Team: n/a");
                    }

                    statsStr.append("\n");
                    statsStr.append(ChatColor.WHITE + "" + ChatColor.BOLD + "╚══[PLAYER STATS]══╝" + ChatColor.RESET);
                    sender.sendMessage(statsStr.toString());
                } else {
                    sender.sendMessage(ChatColor.RED + "That player doesn't exist in the database!");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
