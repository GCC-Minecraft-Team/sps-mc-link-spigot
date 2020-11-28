package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClaimTabCompleter implements TabCompleter {

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("unclaim")) {
            return Collections.emptyList();
        } else if (command.getName().equalsIgnoreCase("claim")) {
            if (args.length == 1) {
                // /claim <partial>
                return CMD.keepStarts(Arrays.asList("chunk", "unchunk", "show", "hide"), args[0]);
            } else {
                // /claim <...>
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
