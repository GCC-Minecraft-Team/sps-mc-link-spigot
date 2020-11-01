package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;

public class PermissionsCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /perms a.k.a. /permissions
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equals("members")) {
            if (args.length == 1) {
                // No arguments for /perms members
                //
            } else if ("" == "") {

            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " members <>");
                return true;
            }
            return false;
        } else if (args[0].equals("rank")) {
            if (args.length == 1) {
                // No arguments for /perms rank
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank <create|delete|list|set|unset|color>");
                return true;
            } else if (args[1].equals("create")) {
                // Creates a new rank and adds it to the list.
                if (args.length != 3) {
                    // Either no arguments for /perms rank create
                    // Or too many (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank create <rank>");
                    return true;
                } else if (SPSSpigot.plugin().perms.getRank(args[2]) != null) {
                    // A rank with this name already exists.
                    sender.sendMessage(ChatColor.RED + "The rank name '" + args[2] + "' is already used.");
                    return true;
                } else {
                    // A valid new rank name is chosen.
                    SPSSpigot.plugin().perms.addRank(new Rank(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Created new rank '" + args[2] + "'!");
                    return true;
                }
            } else if (args[1].equals("delete")) {
                // Deletes a rank and removes it from all players.
                if (args.length != 3) {
                    // Either no arguments for /perms rank delete
                    // Or too many (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank delete <rank>");
                    return true;
                } else if (SPSSpigot.plugin().perms.getRank(args[2]) == null) {
                    // No rank with this name exists.
                    sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                    return true;
                } else {
                    // A valid, existing rank is chosen.
                    SPSSpigot.plugin().perms.deleteRank(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Deleted rank '" + args[2] + "'!");
                    return true;
                }
            } else if (args[1].equals("list")) {
                // Lists either all ranks or all permissions given by a specified rank.
                if (args.length > 3) {
                    // Too many arguments (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank list [rank]");
                    return true;
                } else if (args.length == 2) {
                    // List all ranks
                    StringBuilder str = new StringBuilder(ChatColor.BOLD + "====[RANKS]====\n" + ChatColor.RESET);
                    for (Rank rank : SPSSpigot.plugin().perms.getRanks()) {
                        str.append(rank.getColor()).append(rank.getName()).append(ChatColor.RESET).append("\n");
                    }
                    sender.sendMessage(str.toString());
                    return true;
                } else {
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    if (rank == null) {
                        // No rank with this name exists.
                        sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else {
                        // A valid, existing rank is chosen.
                        StringBuilder str = new StringBuilder(ChatColor.BOLD + "====[" + rank.getColor() + rank.getName() + ChatColor.RESET + ChatColor.BOLD + "]====\n" + ChatColor.RESET);
                        ArrayList<String> sortedPermKeys = new ArrayList<String>(rank.getPerms().keySet());
                        sortedPermKeys.sort(String.CASE_INSENSITIVE_ORDER);
                        for (String perm : sortedPermKeys) {
                            if (rank.hasPermission(perm))
                                str.append(perm).append(ChatColor.GREEN).append(" - TRUE\n").append(ChatColor.RESET);
                            else
                                str.append(perm).append(ChatColor.RED).append(" - FALSE\n").append(ChatColor.RESET);
                        }
                        sender.sendMessage(str.toString());
                        return true;
                    }
                }
            } else if (args[1].equals("set")) {
                // Sets a specified permission for a specified rank to true or false
                if (args.length != 5) {
                    // Not enough arguments or too many arguments given for /perms rank set
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank set <rank> <permission> <true|false>");
                    return true;
                } else {
                    // Get the values
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    Permission perm = SPSSpigot.plugin().getServer().getPluginManager().getPermission(args[3]);
                    Boolean bool = null;
                    if (args[4].equalsIgnoreCase("true"))
                        bool = true;
                    else if (args[4].equalsIgnoreCase("false"))
                        bool = false;
                    if (rank == null) {
                        // Rank not recognized
                        sender.sendMessage(ChatColor.RED + "Rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (perm == null) {
                        // Permission node not recognized
                        sender.sendMessage(ChatColor.RED + "Permission node '" + args[3] + "' was not recognized.");
                        return true;
                    } else if (bool == null) {
                        // Boolean not recognized
                        sender.sendMessage(ChatColor.RED + "Boolean '" + args[4] + "' was not recognized as true or false");
                        return true;
                    } else {
                        // All the data is accounted for so set it.
                        rank.setPermission(perm, bool);
                        sender.sendMessage(ChatColor.GREEN + "Set permission node " + perm.getName() + " to " + bool + " for rank " + rank.getColor() + rank.getName() + ChatColor.RESET + ChatColor.GREEN + "!");
                        return true;
                    }
                }
            } else if (args[1].equals("unset")) {
                // Unsets a specified permission for a specified rank
                if (args.length != 4) {
                    // Not enough arguments or too many arguments given for /perms rank unset
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank unset <rank> <permission>");
                    return true;
                } else {
                    // Get the values
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    Permission perm = SPSSpigot.plugin().getServer().getPluginManager().getPermission(args[3]);
                    if (rank == null) {
                        // Rank not recognized
                        sender.sendMessage(ChatColor.RED + "Rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (perm == null) {
                        // Permission node not recognized
                        sender.sendMessage(ChatColor.RED + "Permission node '" + args[3] + "' was not recognized.");
                        return true;
                    } else {
                        // All the data is accounted for so unset it.
                        rank.unsetPermission(perm);
                        sender.sendMessage(ChatColor.GREEN + "Unset permission node " + perm.getName() + " for rank " + rank.getColor() + rank.getName() + ChatColor.RESET + ChatColor.GREEN + "!");
                        return true;
                    }
                }
            } else if (args[1].equals("color")) {
                // Sets the color for a given rank, or tells you the current color if unspecified.
                if (args.length == 2) {
                    // No arguments given for /perms rank color
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank color <rank> [color]");
                    return true;
                } else {
                    // There is a rank given; may or may not be a color specified.
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    if (rank == null) {
                        // Rank not recognized
                        sender.sendMessage(ChatColor.RED + "Rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (args.length == 3) {
                        // No color specified; report current color
                        sender.sendMessage("Current color for " + rank.getColor() + rank.getName() + ChatColor.RESET + ": " + rank.getColor() + ChatColor.BOLD + rank.getColor().name());
                        return true;
                    } else {
                        // Color specified; change current color
                        ChatColor newColor;
                        try {
                            newColor = ChatColor.valueOf(args[3].toUpperCase().strip());
                        } catch (IllegalArgumentException e) {
                            // Color not recognized
                            sender.sendMessage(ChatColor.RED + "Color '" + args[3] + "' was not recognized. Did you forget to use underscores between words?");
                            return true;
                        }
                        // Change to new color
                        rank.setColor(newColor);
                        sender.sendMessage("Set color for " + newColor + rank.getName() + ChatColor.RESET + ": " + newColor + ChatColor.BOLD + newColor.name());
                        return true;
                    }
                }
            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank <create|delete|list|set|unset|color>");
                return true;
            }

        } else if (args[0].equals("player")) {
            if (args.length == 1) {
                // No arguments for /perms player
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " player <give|remove|info>");
                return true;
            } else if (args[1].equals("give")) {
                // Gives a player a specified rank.
                if (args.length < 4) {
                    // Either no arguments or only rank for /perms player give
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " player give <rank> <player>");
                    return true;
                } else {
                    // There are a given rank and player name.
                    // Get rank - may be null if unrecognized.
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    // Get player - may be null if unrecognized (including offline).
                    StringBuilder playerName = new StringBuilder(args[3]);
                    for (int i = 4; i < args.length; i++) {
                        playerName.append(" ").append(args[i]);
                    }
                    Player player = SPSSpigot.plugin().getServer().getPlayer(playerName.toString());
                    if (rank == null) {
                        // No rank with this name exists.
                        sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (player == null) {
                        // No player with this name exists.
                        sender.sendMessage(ChatColor.RED + "The player '" + playerName + "' was not recognized.");
                        return true;
                    } else {
                        // We have a known player and rank.
                        if (SPSSpigot.plugin().perms.givePlayerRank(player, rank)) {
                            // The player did not have the rank before and we gave them it.
                            sender.sendMessage(ChatColor.GREEN + "Gave rank '" + rank.getName() + "' to player '" + player.getName() + "'!");
                            return true;
                        } else {
                            // The player already had the rank.
                            sender.sendMessage(ChatColor.RED + "Player '" + player.getName() + "' already has rank '" + rank.getName() + "'.");
                            return true;
                        }
                    }
                }
            } else if (args[1].equals("remove")) {
                // Removes a specified rank from a player.
                if (args.length < 4) {
                    // Either no arguments or only rank for /perms player remove
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank player <rank> <player>");
                    return true;
                } else {
                    // There are a given rank and player name.
                    // Get rank - may be null if unrecognized.
                    Rank rank = SPSSpigot.plugin().perms.getRank(args[2]);
                    // Get player - may be null if unrecognized (including offline).
                    StringBuilder playerName = new StringBuilder(args[3]);
                    for (int i = 4; i < args.length; i++) {
                        playerName.append(" ").append(args[i]);
                    }
                    Player player = SPSSpigot.plugin().getServer().getPlayer(playerName.toString());
                    if (rank == null) {
                        // No rank with this name exists.
                        sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (player == null) {
                        // No player with this name exists.
                        sender.sendMessage(ChatColor.RED + "The player '" + playerName + "' was not recognized.");
                        return true;
                    } else {
                        // We have a known player and rank.
                        if (SPSSpigot.plugin().perms.removePlayerRank(player, rank)) {
                            // The player did have the rank before and we removed it.
                            sender.sendMessage(ChatColor.GREEN + "Removed rank '" + rank.getName() + "' from player '" + player.getName() + "'!");
                            return true;
                        } else {
                            // The player did not already have the rank.
                            sender.sendMessage(ChatColor.RED + "Player '" + player.getName() + "' already did not have '" + rank.getName() + "'.");
                            return true;
                        }
                    }
                }
            } else if (args[1].equals("info")) {
                // Gets info on a specific player's ranks
                if (args.length == 2) {
                    // No arguments for /perms player info
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " player info <player>");
                    return true;
                } else {
                    // Get the player they specified
                    StringBuilder str = new StringBuilder(args[2]);
                    for (int i = 3; i < args.length; i++)
                        str.append(args[i]);
                    Player player = SPSSpigot.plugin().getServer().getPlayer(str.toString());
                    if (player == null) {
                        // Player not recognized
                        sender.sendMessage(ChatColor.RED + "Player '" + str + "' was not recognized.");
                        return true;
                    } else {
                        // Give them the info
                        // Header
                        StringBuilder out = new StringBuilder(ChatColor.BOLD + "====[" + player.getName() + "]====\n" + ChatColor.RESET);
                        // SPS info
                        if (DatabaseLink.isRegistered(player.getUniqueId()))
                            out.append(ChatColor.ITALIC + DatabaseLink.getSPSName(player.getUniqueId()) + "\n" + ChatColor.RESET);
                        else
                            out.append(ChatColor.ITALIC + "SPS profile unlinked\n" + ChatColor.RESET);
                        // Player ranks
                        for (Rank rank : SPSSpigot.plugin().perms.getPlayerRanks(player))
                            out.append(rank.getColor() + rank.getName() + ChatColor.RESET + "\n");
                        sender.sendMessage(out.toString());
                        return true;
                    }
                }
            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + "player <give|remove|info>");
                return true;
            }
        } else if (args[0].equals("reload")) {
            // Reloads perms from file.
            SPSSpigot.plugin().perms.loadFile();
            sender.sendMessage(ChatColor.GREEN + "Reloaded permissions!");
            return true;
        } else {
            // arg[0] is invalid
            return false;
        }
    }
}
