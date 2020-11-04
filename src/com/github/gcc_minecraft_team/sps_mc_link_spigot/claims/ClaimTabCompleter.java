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
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // /claim <partial>
            return Arrays.asList("chunk", "unchunk");
        } else {
            // /claim <...>
            return new ArrayList<>();
        }
    }
}
