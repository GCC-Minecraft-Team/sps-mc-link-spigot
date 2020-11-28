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

public class AdminClaimTabCompleter implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // /adminc <partial>
            return CMD.keepStarts(Arrays.asList("override", "unclaim"), args[0]);
        } else if (args[1].equalsIgnoreCase("override")) {
            // /adminc override <...partial>
            return Collections.emptyList();
        } else if (args[1].equalsIgnoreCase("unclaim")) {
            // /adminc unclaim <...partial>
            return Collections.emptyList();
        } else {
            // /adminc <INVALID> <...partial>
            return Collections.emptyList();
        }
    }
}
