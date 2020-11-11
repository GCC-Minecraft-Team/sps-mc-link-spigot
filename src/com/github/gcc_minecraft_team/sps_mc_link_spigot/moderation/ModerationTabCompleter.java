package com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.haoshoku.nick.api.NickAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModerationTabCompleter implements TabCompleter {
    
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
        // /mod (moderation commands)
        if (args.length == 1) {
            // /mod <partial>
            return CMD.keepStarts(Arrays.asList("banSPS", "tpSPS", "muteSPS", "unmuteSPS"), args[0]);
        } else if (args[0].equals("banSPS") || args[0].equals("muteSPS") || args[0].equals("unmuteSPS")) {
            if (args.length == 2) {
                // /mod banSPS <partial>
                return CMD.keepStarts(new ArrayList<>(DatabaseLink.getAllSPSNames()), args[1]);
            }
        } else if (args[0].equals("tpSPS")) {
            if (args.length == 2) {
                // /mod tpSPS <partial>
                return CMD.keepStarts(new ArrayList<>(DatabaseLink.getPlayerNames()), args[1]);
            }
        }

        return new ArrayList<>();
    }
}
