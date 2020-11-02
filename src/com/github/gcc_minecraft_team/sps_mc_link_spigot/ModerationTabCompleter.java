package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import xyz.haoshoku.nick.api.NickAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModerationTabCompleter implements TabCompleter {

    public static List<String> keepStarts(List<String> list, String prefix) {
        List<String> newList = new ArrayList<>();
        for (String str : list) {
            if (str.toLowerCase().startsWith(prefix.toLowerCase()))
                newList.add(str);
        }
        newList.sort(String.CASE_INSENSITIVE_ORDER);
        return newList;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // /mod (moderation commands)
        if (args.length == 1) {
            // /mod <partial>
            return keepStarts(Arrays.asList("banSPS"), args[0]);
        } else if (args[0].equals("banSPS")) {
            if (args.length == 2) {
                // /mod banSPS <partial>
                return keepStarts(new ArrayList<String>(NickAPI.getNickedPlayers().values()), args[1]);
            }
        }

        return new ArrayList<>();
    }
}
