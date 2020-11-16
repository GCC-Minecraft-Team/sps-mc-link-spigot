package com.github.gcc_minecraft_team.sps_mc_link_spigot.general;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GeneralTabCompleter implements TabCompleter {

    /**
     * Fires when the player presses tab to autocomplete the command
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("stats")) {
            if (args.length == 1) {
                return CMD.keepStarts(new ArrayList<>(DatabaseLink.getAllSPSNames()), args[0]);
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }
}
