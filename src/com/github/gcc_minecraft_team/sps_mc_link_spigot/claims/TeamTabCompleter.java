package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamTabCompleter implements TabCompleter {

    public static List<String> keepStarts(List<String> list, String prefix) {
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
        if (args.length == 1) {
            // /team <partial>
            return keepStarts(Arrays.asList("create", "join", "leave", "list"), args[0]);
        } else if (args[0].equals("create")) {
            // /team create <...partial>
            return new ArrayList<>();
        } else if (args[0].equals("join")) {
            if (args.length == 2) {
                // /team join <partial>
                return keepStarts(new ArrayList<>(SPSSpigot.claims().getTeamNames()), args[1]);
            } else {
                // /team join <team> <partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("leave")) {
            // /team leave <partial>
            return new ArrayList<>();
        } else if (args[0].equals("list")) {
            if (args.length == 2) {
                // /team list <partial>
                return keepStarts(new ArrayList<>(SPSSpigot.claims().getTeamNames()), args[1]);
            } else {
                // /team list <team> <partial>
                return new ArrayList<>();
            }
        } else {
            // /team <INVALID>
            return new ArrayList<>();
        }
    }
}
