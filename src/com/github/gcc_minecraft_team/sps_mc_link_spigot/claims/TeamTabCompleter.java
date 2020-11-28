package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamTabCompleter implements TabCompleter {

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
        if (worldGroup == null) {
            // This world is not in a WorldGroup
            return Collections.emptyList();
        } else {
            Team team = worldGroup.getPlayerTeam(player.getUniqueId());
            if (args.length == 1) {
                // /team <partial>
                return CMD.keepStarts(Arrays.asList("create", "join", "leave", "kick", "list", "requests"), args[0]);
            } else if (args[0].equalsIgnoreCase("create")) {
                // /team create <...partial>
                return Collections.emptyList();
            } else if (args[0].equalsIgnoreCase("join")) {
                if (args.length == 2 && team == null) {
                    // /team join <partial>
                    // And not on a team
                    return CMD.keepStarts(new ArrayList<>(worldGroup.getTeamNames()), args[1]);
                } else {
                    // /team join <team> <partial>
                    // Or player is already on a team
                    return Collections.emptyList();
                }
            } else if (args[0].equalsIgnoreCase("kick")) {
                // /team kick <...partial>
                if (team != null)
                    return CMD.keepStarts(new ArrayList<>(team.getMemberNames()), args[1]);
                else
                    return Collections.emptyList();
            } else if (args[0].equalsIgnoreCase("leave")) {
                // /team leave <partial>
                return Collections.emptyList();
            } else if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 2) {
                    // /team list <partial>
                    return CMD.keepStarts(new ArrayList<>(worldGroup.getTeamNames()), args[1]);
                } else {
                    // /team list <team> <partial>
                    return Collections.emptyList();
                }
            } else if (args[0].equalsIgnoreCase("requests")) {
                if (args.length == 2) {
                    // /team requests <partial>
                    return CMD.keepStarts(Arrays.asList("list", "accept", "deny"), args[1]);
                } else if (args[1].equalsIgnoreCase("list")) {
                    // /team requests list <...partial>
                    return Collections.emptyList();
                } else if (args[1].equalsIgnoreCase("accept")) {
                    if (args.length == 3) {
                        // /team requests accept <partial>
                        if (team != null && team.getLeader().equals(player.getUniqueId())) {
                            // Player is the leader of team
                            List<String> names = new ArrayList<>();
                            for (UUID uuid : worldGroup.getTeamJoinRequests(team))
                                names.add(DatabaseLink.getSPSName(uuid));
                            return CMD.keepStarts(names, args[2]);
                        } else {
                            // Player is not on a team or is not the leader
                            return Collections.emptyList();
                        }
                    } else {
                        // /team requests accept <name> <...partial>
                        return Collections.emptyList();
                    }
                } else if (args[1].equalsIgnoreCase("deny")) {
                    if (args.length == 3) {
                        // /team requests deny <partial>
                        if (team != null && team.getLeader().equals(player.getUniqueId())) {
                            // Player is the leader of team
                            List<String> names = new ArrayList<>();
                            for (UUID uuid : worldGroup.getTeamJoinRequests(team))
                                names.add(DatabaseLink.getSPSName(uuid));
                            return CMD.keepStarts(names, args[2]);
                        } else {
                            // Player is not on a team or is not the leader
                            return Collections.emptyList();
                        }
                    } else {
                        // /team requests deny <name> <...partial>
                        return Collections.emptyList();
                    }
                } else {
                    // /team requests <INVALID>
                    return Collections.emptyList();
                }
            } else {
                // /team <INVALID>
                return Collections.emptyList();
            }
        }
    }
}
