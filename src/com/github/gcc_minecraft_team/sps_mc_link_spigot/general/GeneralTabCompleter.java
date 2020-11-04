package com.github.gcc_minecraft_team.sps_mc_link_spigot.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GeneralTabCompleter implements TabCompleter {

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

    /**
     * Fires when the player presses tab to autocomplete the command
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
