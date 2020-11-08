package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TeamTabCompleter implements TabCompleter {

    @NotNull
    public static List<String> keepStarts(@NotNull List<String> list, @NotNull String prefix) {
        List<String> newList = new ArrayList<>();
        for (String str : list) {
            if (str.toLowerCase().startsWith(prefix.toLowerCase()))
                newList.add(str);
        }
        newList.sort(String.CASE_INSENSITIVE_ORDER);
        return newList;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        Player player = (Player) sender;
        ClaimHandler worldGroup = SPSSpigot.claims(player.getWorld());
        if (worldGroup == null) {
            // This world is not in a worldGroup
            return new ArrayList<>();
        } else if (args.length == 1) {
            // /team <partial>
            return keepStarts(Arrays.asList("create", "join", "leave", "kick", "list", "requests"), args[0]);
        } else if (args[0].equals("create")) {
            // /team create <...partial>
            return new ArrayList<>();
        } else if (args[0].equals("join")) {
            if (args.length == 2) {
                // /team join <partial>
                return keepStarts(new ArrayList<>(worldGroup.getTeamNames()), args[1]);
            } else {
                // /team join <team> <partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("kick")) {
            // /team kick <...partial>
            return keepStarts(new ArrayList<>(worldGroup.getPlayerTeam((player).getUniqueId()).getMemberNames()), args[1]);
        } else if (args[0].equals("leave")) {
            // /team leave <partial>
            return new ArrayList<>();
        } else if (args[0].equals("list")) {
            if (args.length == 2) {
                // /team list <partial>
                return keepStarts(new ArrayList<>(worldGroup.getTeamNames()), args[1]);
            } else {
                // /team list <team> <partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("requests")) {
            Team team = worldGroup.getPlayerTeam(player.getUniqueId());
            if (args.length == 2) {
                // /team requests <partial>
                return keepStarts(Arrays.asList("list", "accept", "deny"), args[1]);
            } else if (args[1].equals("list")) {
                // /team requests list <...partial>
                return new ArrayList<>();
            } else if (args[1].equals("accept")) {
                if (args.length == 3) {
                    // /team requests accept <partial>
                    if (team != null && team.getLeader().equals(player.getUniqueId())) {
                        // Player is the leader of team
                        List<String> names = new ArrayList<>();
                        for (UUID uuid : worldGroup.getTeamJoinRequests(team))
                            names.add(DatabaseLink.getSPSName(uuid));
                        return keepStarts(names, args[2]);
                    } else {
                        // Player is not on a team or is not the leader
                        return new ArrayList<>();
                    }
                } else {
                    // /team requests accept <name> <...partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("deny")) {
                if (args.length == 3) {
                    // /team requests deny <partial>
                    if (team != null && team.getLeader().equals(player.getUniqueId())) {
                        // Player is the leader of team
                        List<String> names = new ArrayList<>();
                        for (UUID uuid : worldGroup.getTeamJoinRequests(team))
                            names.add(DatabaseLink.getSPSName(uuid));
                        return keepStarts(names, args[2]);
                    } else {
                        // Player is not on a team or is not the leader
                        return new ArrayList<>();
                    }
                } else {
                    // /team requests deny <name> <...partial>
                    return new ArrayList<>();
                }
            } else {
                // /team requests <INVALID>
                return new ArrayList<>();
            }
        } else {
            // /team <INVALID>
            return new ArrayList<>();
        }
    }
}
