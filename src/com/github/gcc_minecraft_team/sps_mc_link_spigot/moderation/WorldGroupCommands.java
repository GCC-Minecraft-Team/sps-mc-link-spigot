package com.github.gcc_minecraft_team.sps_mc_link_spigot.moderation;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.claims.WorldGroup;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorldGroupCommands implements CommandExecutor {

    public static final String adminPrefix = ChatColor.AQUA + "[SPSMC Admin System]: " + ChatColor.WHITE;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // No arguments for /wgroup
            return false;
        } else if (args[0].equals("create")) {
            if (args.length != 2) {
                // Either no arguments or too many for /wgroup create <name>
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " create <name>");
                return true;
            } else {
                if (SPSSpigot.plugin().addWorldGroup(new WorldGroup(args[1]))) {
                    // Success
                    sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully created new world group " + args[1] + ".");
                    return true;
                } else {
                    // Name taken
                    sender.sendMessage(adminPrefix + ChatColor.RED + "The world group name '" + args[1] + "' was already taken.");
                    return true;
                }
            }
        } else if (args[0].equals("delete")) {
            // arg[2] can equal "-confirm=<CASE-SENSITIVE NAME>" to skip confirmation. This is suggested by the tellraw in confirmation to force the player to rewrite the name.
            if (args.length == 3 && args[2].startsWith("-confirm=")) {
                // /wgroup delete <NON-CASE-SENSITIVE NAME> -confirm=<CASE-SENSITIVE NAME>
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else if (args[2].equals("-confirm=" + worldGroup.getName())) {
                    SPSSpigot.plugin().removeWorldGroup(worldGroup);
                    sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully deleted world group " + worldGroup.getName() + "!");
                    return true;
                } else {
                    // This means they just put in the wrong confirmation. Tell them to fix it.
                    sender.sendMessage(adminPrefix + ChatColor.RED + "Invalid confirmation. Please ensure you pay attention to case: '-confirm=" + worldGroup.getName() + "'");
                    return true;
                }
            } else if (args.length != 2) {
                // This excludes args[2] starting with "-confirm="
                // Either no arguments or too many for /wgroup delete <world group>
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " delete <world group>");
                return true;
            } else {
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else {
                    TextComponent message1 = new TextComponent("To confirm, ");
                    message1.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                    TextComponent message2 = new TextComponent("[CLICK]");
                    message2.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                    message2.setBold(true);
                    message2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + label + " delete " + worldGroup.getName() + " -confirm="));
                    TextComponent message3 = new TextComponent(" and type out the case-sensitive name of this world group:\n");
                    message3.setColor(net.md_5.bungee.api.ChatColor.GOLD);

                    TextComponent message4 = new TextComponent("/" + label + " delete " + worldGroup.getName() + " -confirm=");
                    message4.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
                    TextComponent message5 = new TextComponent(worldGroup.getName());
                    message5.setColor(net.md_5.bungee.api.ChatColor.RED);

                    sender.spigot().sendMessage(message1, message2, message3, message4, message5);
                    return true;
                }
            }
        } else if (args[0].equals("addworld")) {
            // This one does not need confirmation because you can't add a taken world
            if (args.length != 3) {
                // Incorrect number of arguments for /wgroup addworld <world group> <world>
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " addworld <world group> <world>");
                return true;
            } else {
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                World world = SPSSpigot.server().getWorld(args[2]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else if (world == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[2] + "' was not recognized.");
                    return true;
                } else {
                    if (worldGroup.addWorld(world)) {
                        sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully added world " + world.getName() + " to " + worldGroup.getName() + "!");
                        return true;
                    } else {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World " + world.getName() + " is already in a world group and could not be added.");
                        return true;
                    }
                }
            }
        } else if (args[0].equals("setworldspawn")) {
            if (args.length != 3) {
                // Incorrect number of arguments for /wgroup setworldspawn <world group> <world>
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " setworldspawn <world group> <world>");
                return true;
            } else {
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                World world = SPSSpigot.server().getWorld(args[2]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else if (world == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[2] + "' was not recognized.");
                    return true;
                } else {
                    worldGroup.setWorldSpawnLocation(world.getUID(), ((Player) sender).getLocation());
                    sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully set world " + world.getName() + " spawn to " + worldGroup.getSpawnLocations().get(world.getUID()).toString() + "!");
                    return true;
                }
            }
        } else if (args[0].equals("remworld")) {
            // arg[3] can equal "-confirm=<CASE-SENSITIVE WORLD NAME>" to skip confirmation. This is suggested by the tellraw in confirmation to force the player to rewrite the name.
            if (args.length == 4 && args[3].startsWith("-confirm=")) {
                // /wgroup remworld <WORLD GROUP> <NON-CASE-SENSITIVE WORLD NAME> -confirm=<CASE-SENSITIVE WORLD NAME>
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                World world = SPSSpigot.server().getWorld(args[2]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else if (world == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[2] + "' was not recognized.");
                    return true;
                } else if (args[3].equals("-confirm=" + world.getName())) {
                    worldGroup.removeWorld(world);
                    sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully removed world " + worldGroup.getName() + " from " + worldGroup.getName() + "!");
                    return true;
                } else {
                    // This means they just put in the wrong confirmation. Tell them to fix it.
                    sender.sendMessage(adminPrefix + ChatColor.RED + "Invalid confirmation. Please ensure you pay attention to case: '-confirm=" + worldGroup.getName() + "'");
                    return true;
                }
            } else if (args.length != 3) {
                // This excludes args[3] starting with "-confirm="
                // Either no arguments or too many for /wgroup remworld <world group> <world>
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " remworld <world group> <world>");
                return true;
            } else {
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                World world = SPSSpigot.server().getWorld(args[2]);
                if (worldGroup == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[1] + "' was not recognized.");
                    return true;
                } else if (world == null) {
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[2] + "' was not recognized.");
                    return true;
                } else {
                    TextComponent message1 = new TextComponent("To confirm, ");
                    message1.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                    TextComponent message2 = new TextComponent("[CLICK]");
                    message2.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                    message2.setBold(true);
                    message2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + label + " remworld " + worldGroup.getName() + " " + world.getName() + " -confirm="));
                    TextComponent message3 = new TextComponent(" and type out the case-sensitive name of the world:\n");
                    message3.setColor(net.md_5.bungee.api.ChatColor.GOLD);

                    TextComponent message4 = new TextComponent("/" + label + " remworld " + worldGroup.getName() + " " + world.getName() + " -confirm=");
                    message4.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
                    TextComponent message5 = new TextComponent(world.getName());
                    message5.setColor(net.md_5.bungee.api.ChatColor.RED);

                    sender.spigot().sendMessage(message1, message2, message3, message4, message5);
                    return true;
                }
            }
        } else if (args[0].equals("claimable")) {
            if (args[1].equals("addworld")) {
                // This one does not need confirmation because you can't add a taken world
                if (args.length != 4) {
                    // Incorrect number of arguments for /wgroup claimable addworld <world group> <world>
                    sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " claimable addworld <world group> <world>");
                    return true;
                } else {
                    WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[2]);
                    World world = SPSSpigot.server().getWorld(args[3]);
                    if (worldGroup == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (world == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[3] + "' was not recognized.");
                        return true;
                    } else {
                        if (worldGroup.addWorldClaimable(world)) {
                            sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully added world " + world.getName() + " as claimable to " + worldGroup.getName() + "!");
                            return true;
                        } else {
                            sender.sendMessage(adminPrefix + ChatColor.RED + "World " + world.getName() + " has not been added to " + worldGroup.getName() + "!");
                            return true;
                        }
                    }
                }
            } else if (args[1].equals("remworld")) {
                // arg[3] can equal "-confirm=<CASE-SENSITIVE WORLD NAME>" to skip confirmation. This is suggested by the tellraw in confirmation to force the player to rewrite the name.
                if (args.length == 4) {
                    // /wgroup claimable remworld <WORLD GROUP> <NON-CASE-SENSITIVE WORLD NAME> -confirm=<CASE-SENSITIVE WORLD NAME>
                    WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[2]);
                    World world = SPSSpigot.server().getWorld(args[3]);
                    if (worldGroup == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (world == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[3] + "' was not recognized.");
                        return true;
                    } else {
                        worldGroup.removeWorldClaimable(world);
                        sender.sendMessage(adminPrefix + ChatColor.GREEN + "Successfully disabled claiming for world " + worldGroup.getName() + " from " + worldGroup.getName() + "!");
                        return true;
                    }
                } else if (args.length != 4) {
                    // This excludes args[3] starting with "-confirm="
                    // Either no arguments or too many for /wgroup remworld <world group> <world>
                    sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " claimable remworld <world group> <world>");
                    return true;
                } else {
                    WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[2]);
                    World world = SPSSpigot.server().getWorld(args[3]);
                    if (worldGroup == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (world == null) {
                        sender.sendMessage(adminPrefix + ChatColor.RED + "World '" + args[3] + "' was not recognized.");
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else if (args[0].equals("list")) {
            if (args.length == 1) {
                // List all world groups
                List<String> names = new ArrayList<>();
                for (WorldGroup worldGroup : SPSSpigot.plugin().getWorldGroups())
                    names.add(worldGroup.getName());
                sender.spigot().sendMessage(CMD.buildListTellraw("WORLD GROUPS", names, "/wgroup list %s"));
                return true;
            } else if (args.length == 2) {
                // List all worlds in a given group
                WorldGroup worldGroup = SPSSpigot.plugin().getWorldGroup(args[1]);
                if (worldGroup == null) {
                    // World group not recognized.
                    sender.sendMessage(adminPrefix + ChatColor.RED + "World group '" + args[0] + "' was not recognized.");
                    return true;
                } else {
                    List<String> names = new ArrayList<>();
                    for (World world : worldGroup.getWorlds())
                        names.add(world.getName());
                    sender.sendMessage(CMD.buildListText("WORLDS", names));
                    return true;
                }
            } else {
                // Invalid number of arguments
                sender.sendMessage(adminPrefix + ChatColor.RED + "Usage: /" + label + " list [world group]");
                return true;
            }
        } else {
            // args[0] is invalid
            return false;
        }
    }
}
