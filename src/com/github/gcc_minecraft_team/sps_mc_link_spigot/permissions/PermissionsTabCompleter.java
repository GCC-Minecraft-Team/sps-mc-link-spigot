package com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsTabCompleter implements TabCompleter {
    
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // /perms a.k.a. /permissions
        if (args.length == 1) {
            // /perms <partial>
            return CMD.keepStarts(Arrays.asList("members", "rank", "player", "reload"), args[0]);
        } else if (args[0].equals("members")) {
            if (args.length == 2) {
                // /perms members <partial>
                return CMD.keepStarts(Arrays.asList("set", "unset", "list"), args[1]);
            } else if (args[1].equals("set")) {
                if (args.length == 3) {
                    // /perms members set <partial>
                    List<String> perms = new ArrayList<>();
                    for (Permission p : SPSSpigot.server().getPluginManager().getPermissions())
                        perms.add(p.getName());
                    return CMD.keepStarts(perms, args[2]);
                } else if (args.length == 4) {
                    // /perms members set <permission> <partial>
                    return CMD.keepStarts(Arrays.asList("true", "false"), args[3]);
                } else {
                    // /perms members set <permission> <true|false> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("unset")) {
                if (args.length == 3) {
                    // /perms members unset <partial>
                    List<String> perms = new ArrayList<>();
                    for (Permission s : SPSSpigot.perms().getMemberPerms().keySet())
                        perms.add(s.getName());
                    return CMD.keepStarts(perms, args[2]);
                } else {
                    // perms members unset <permission> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("list")) {
                // /perms members list <partial>
                return new ArrayList<>();
            } else {
                // /perms members <INVALID> <...>
                return new ArrayList<>();
            }
        } else if (args[0].equals("rank")) {
            if (args.length == 2) {
                // /perms rank <partial>
                return CMD.keepStarts(Arrays.asList("create", "delete", "list", "set", "unset", "color"), args[1]);
            } else if (args[1].equals("create")) {
                // /perms rank create <...partial>
                // Rank name is not from a list.
                return new ArrayList<>();
            } else if (args[1].equals("delete")) {
                if (args.length == 3) {
                    // /perms rank delete <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else {
                    // /perms rank delete <...> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("list")) {
                if (args.length == 3) {
                    // /perms rank list <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else {
                    // /perms rank list <...> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("set")) {
                if (args.length == 3) {
                    // /perms rank set <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else if (args.length == 4) {
                    // /perms rank set <rank> <partial>
                    List<String> perms = new ArrayList<>();
                    for (Permission p : SPSSpigot.server().getPluginManager().getPermissions())
                        perms.add(p.getName());
                    return CMD.keepStarts(perms, args[3]);
                } else if (args.length == 5) {
                    // perms rank set <rank> <permission> <partial>
                    return CMD.keepStarts(Arrays.asList("true", "false"), args[4]);
                } else {
                    // perms rank set <rank> <permission> <true|false> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("unset")) {
                if (args.length == 3) {
                    // /perms rank unset <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else if (args.length == 4) {
                    // /perms rank unset <rank> <partial>
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    if (rank == null) {
                        return new ArrayList<>();
                    } else {
                        // Get all the permissions that are set for this rank.
                        List<String> permStrs = new ArrayList<>();
                        for (Permission perm : rank.getPerms().keySet())
                            permStrs.add(perm.getName());
                        return CMD.keepStarts(permStrs, args[3]);
                    }
                } else {
                    // perms rank unset <rank> <permission> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("color")) {
                if (args.length == 3) {
                    // /perms rank color <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else if (args.length == 4) {
                    // /perms rank color <rank> <partial>
                    List<String> colorNames = new ArrayList<>();
                    for (ChatColor color : ChatColor.values())
                        colorNames.add(color.name());
                    return CMD.keepStarts(colorNames, args[3]);
                } else {
                    // /perms rank color <rank> <color> <partial>
                    return new ArrayList<>();
                }
            } else {
                // /perms rank <INVALID> <...>
                return new ArrayList<>();
            }
        } else if (args[0].equals("player")) {
            if (args.length == 2) {
                // /perms player <partial>
                return CMD.keepStarts(Arrays.asList("give", "remove", "info"), args[1]);
            } else if (args[1].equals("give")) {
                if (args.length == 3) {
                    // /perms player give <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else if (args.length == 4) {
                    // /perms player give <rank> <partial>
                    return CMD.keepStarts(new ArrayList<>(DatabaseLink.getAllSPSNames()), args[3]); // sps player name
                } else {
                    // /perms player give <rank> <...> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("remove")) {
                if (args.length == 3) {
                    // /perms player remove <partial>
                    return CMD.keepStarts(new ArrayList<>(SPSSpigot.perms().getRankNames()), args[2]);
                } else if (args.length == 4) {
                    // /perms player remove <rank> <partial>
                    return CMD.keepStarts(new ArrayList<>(DatabaseLink.getAllSPSNames()), args[3]); // sps player name
                } else {
                    // /perms player remove <rank> <...> <partial>
                    return new ArrayList<>();
                }
            } else if (args[1].equals("info")) {
                if (args.length == 3) {
                    // /perms player info <partial>
                    return CMD.keepStarts(new ArrayList<>(DatabaseLink.getAllSPSNames()), args[2]); // sps player name
                } else {
                    // /perms player info <...> <partial>
                    return new ArrayList<>();
                }
            } else {
                // /perms player <INVALID> <...>
                return new ArrayList<>();
            }
        } else if (args[0].equals("reload")) {
            return new ArrayList<>();
        } else {
            // /perms <INVALID> <...>
            return new ArrayList<>();
        }
    }
}
