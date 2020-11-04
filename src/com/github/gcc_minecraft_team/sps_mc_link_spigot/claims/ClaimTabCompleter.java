package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClaimTabCompleter implements TabCompleter {

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
        if (args.length == 1) {
            // /claim <partial>
            return keepStarts(Arrays.asList("chunk", "unchunk"), args[0]);
        } else {
            // /claim <...>
            return new ArrayList<>();
        }
    }
}
