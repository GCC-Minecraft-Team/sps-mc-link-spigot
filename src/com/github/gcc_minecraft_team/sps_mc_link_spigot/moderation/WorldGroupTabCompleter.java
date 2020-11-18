package com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldGroupTabCompleter implements TabCompleter {

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // /wgroup <partial>
            return CMD.keepStarts(Arrays.asList("create", "delete", "addworld", "remworld", "claimable", "list", "setworldspawn"), args[0]);
        } else if (args[0].equals("create")) {
            // /wgroup create <...partial>
            return new ArrayList<>();
        } else if (args[0].equals("delete")) {
            if (args.length == 2) {
                // /wgroup delete <partial>
                List<String> names = new ArrayList<>();
                for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
                    names.add(worldGroup.getName());
                return CMD.keepStarts(names, args[1]);
            } else {
                // /wgroup delete <world group> <...partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("addworld")) {
            if (args.length == 2) {
                // /wgroup addworld <partial>
                List<String> names = new ArrayList<>();
                for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
                    names.add(worldGroup.getName());
                return CMD.keepStarts(names, args[1]);
            } else if (args.length == 3) {
                // /wgroup addworld <world group> <partial>
                List<String> names = new ArrayList<>();
                for (World world : SPSSpigot.server().getWorlds())
                    names.add(world.getName());
                return CMD.keepStarts(names, args[2]);
                // TODO: Decide if we exclude worlds already in world groups
            } else {
                // /wgroup addworld <world group> <world> <...partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("remworld")) {
            if (args.length == 2) {
                // /wgroup remworld <partial>
                List<String> names = new ArrayList<>();
                for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
                    names.add(worldGroup.getName());
                return CMD.keepStarts(names, args[1]);
            } else if (args.length == 3) {
                // /wgroup remworld <world group> <partial>
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                if (worldGroup == null) {
                    return new ArrayList<>();
                } else {
                    List<String> names = new ArrayList<>();
                    for (World world : worldGroup.getWorlds())
                        names.add(world.getName());
                    return CMD.keepStarts(names, args[2]);
                }
            } else {
                // /wgroup remworld <world group> <world> <...partial>
                return new ArrayList<>();
            }
        } else if (args[0].equals("claimable")) {
            if (args.length == 2) {
                // wgroup claimable
                return CMD.keepStarts(Arrays.asList("addworld", "remworld"), args[1]);
            } else {
                return new ArrayList<>();
            }
        } else if (args[0].equals("list")) {
            if (args.length == 2) {
                // /wgroup list <partial>
                List<String> names = new ArrayList<>();
                for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
                    names.add(worldGroup.getName());
                return CMD.keepStarts(names, args[1]);
            } else {
                // /wgroup list <world group> <...partial>
                return new ArrayList<>();
            }
        } else {
            // /wgroup <INVALID> <...partial>
            return new ArrayList<>();
        }
    }
}
