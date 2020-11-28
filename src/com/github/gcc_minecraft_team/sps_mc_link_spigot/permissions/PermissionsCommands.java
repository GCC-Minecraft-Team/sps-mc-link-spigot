package com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /perms a.k.a. /permissions
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equalsIgnoreCase("members")) {
            if (args.length == 1) {
                // No arguments for /perms members
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " members <set|unset|list>");
                return true;
            } else if (args[1].equalsIgnoreCase("set")) {
                if (args.length != 4) {
                    // Wrong number of arguments for /perms members set
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " members set <permission> <true|false>");
                    return true;
                } else {
                    // Parse arguments
                    Permission perm = SPSSpigot.server().getPluginManager().getPermission(args[2]);
                    Boolean bool = null;
                    if (args[3].equalsIgnoreCase("true"))
                        bool = true;
                    else if (args[3].equalsIgnoreCase("false"))
                        bool = false;
                    // Checks
                    if (perm == null) {
                        // Permission node not recognized
                        sender.sendMessage(ChatColor.RED + "Permission node '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (bool == null) {
                        // Boolean not recognized
                        sender.sendMessage(ChatColor.RED + "Boolean '" + args[3] + "' was not recognized as true or false.");
                        return true;
                    } else {
                        // Set member perms
                        SPSSpigot.perms().setMemberPerm(perm, bool);
                        sender.sendMessage(ChatColor.GREEN + "Set permission node " + perm.getName() + " to " + bool + " for all members.");
                        return true;
                    }
                }
            } else if (args[1].equalsIgnoreCase("unset")) {
                if (args.length != 3) {
                    // Wrong number of arguments for /perms members unset
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " members unset <permission>");
                    return true;
                } else {
                    // Parse arguments
                    Permission perm = SPSSpigot.server().getPluginManager().getPermission(args[2]);
                    // Checks
                    if (perm == null) {
                        // Permission node not recognized
                        sender.sendMessage(ChatColor.RED + "Permission node '" + args[2] + "' was not recognized.");
                        return true;
                    } else {
                        // Set member perms
                        SPSSpigot.perms().unsetMemberPerm(perm);
                        sender.sendMessage(ChatColor.GREEN + "Unset permission node " + perm.getName() + " to for all members.");
                        return true;
                    }
                }
            } else if (args[1].equalsIgnoreCase("list")) {
                // Send full list of permissions
                Map<String, Boolean> perms = new HashMap<>();
                for (Map.Entry<Permission, Boolean> p : SPSSpigot.perms().getMemberPerms().entrySet())
                    perms.put(p.getKey().getName(), p.getValue());
                sender.spigot().sendMessage(CMD.buildListBooleanTellraw("MEMBERS", perms, "/perms members set %s %b"));
                return true;
            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " members <set|unset|list>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("rank")) {
            if (args.length == 1) {
                // No arguments for /perms rank
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank <create|delete|list|set|unset|color>");
                return true;
            } else if (args[1].equalsIgnoreCase("create")) {
                // Creates a new rank and adds it to the list.
                if (args.length != 3) {
                    // Either no arguments for /perms rank create
                    // Or too many (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank create <rank>");
                    return true;
                } else if (SPSSpigot.perms().getRank(args[2]) != null) {
                    // A rank with this name already exists.
                    sender.sendMessage(ChatColor.RED + "The rank name '" + args[2] + "' is already used.");
                    return true;
                } else {
                    // A valid new rank name is chosen.
                    SPSSpigot.perms().addRank(new Rank(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Created new rank '" + args[2] + "'!");
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("delete")) {
                // Deletes a rank and removes it from all players.
                if (args.length != 3) {
                    // Either no arguments for /perms rank delete
                    // Or too many (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank delete <rank>");
                    return true;
                } else if (SPSSpigot.perms().getRank(args[2]) == null) {
                    // No rank with this name exists.
                    sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                    return true;
                } else {
                    // A valid, existing rank is chosen.
                    SPSSpigot.perms().deleteRank(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Deleted rank '" + args[2] + "'!");
                    return true;
                }
            } else if (args[1].equalsIgnoreCase("list")) {
                // Lists either all ranks or all permissions given by a specified rank.
                if (args.length > 3) {
                    // Too many arguments (multi-word rank name)
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank list [rank]");
                    return true;
                } else if (args.length == 2) {
                    // List all ranks
                    sender.spigot().sendMessage(CMD.buildListTellraw("RANKS", new ArrayList<>(SPSSpigot.perms().getRankNames()), "/perms rank list %s"));
                    return true;
                } else {
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    if (rank == null) {
                        // No rank with this name exists.
                        sender.sendMessage(ChatColor.RED + "The rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else {
                        // A valid, existing rank is chosen.
                        Map<String, Boolean> permStrs = new HashMap<>();
                        for (Map.Entry<Permission, Boolean> perm : rank.getPerms().entrySet())
                            permStrs.put(perm.getKey().getName(), perm.getValue());
                        sender.spigot().sendMessage(CMD.buildListBooleanTellraw(rank.getColor() + rank.getName(), permStrs, "/perms rank set " + rank.getName() + " %s %b"));
                        return true;
                    }
                }
            } else if (args[1].equalsIgnoreCase("set")) {
                // Sets a specified permission for a specified rank to true or false
                if (args.length != 5) {
                    // Not enough arguments or too many arguments given for /perms rank set
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank set <rank> <permission> <true|false>");
                    return true;
                } else {
                    // Get the values
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    Permission perm = SPSSpigot.server().getPluginManager().getPermission(args[3]);
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
            } else if (args[1].equalsIgnoreCase("unset")) {
                // Unsets a specified permission for a specified rank
                if (args.length != 4) {
                    // Not enough arguments or too many arguments given for /perms rank unset
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank unset <rank> <permission>");
                    return true;
                } else {
                    // Get the values
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    Permission perm = SPSSpigot.server().getPluginManager().getPermission(args[3]);
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
            } else if (args[1].equalsIgnoreCase("color")) {
                // Sets the color for a given rank, or tells you the current color if unspecified.
                if (args.length == 2) {
                    // No arguments given for /perms rank color
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank color <rank> [color]");
                    return true;
                } else {
                    // There is a rank given; may or may not be a color specified.
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
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
            } else if (args[1].equalsIgnoreCase("claims")) {
                // sets the number of extra claims a rank should have.
                if (args.length == 2) {
                    // No arguments given for /perms rank color
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank claims <rank> [extra claims]");
                    return true;
                } else {
                    // There is a rank given; may or may not be a number specified.
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    if (rank == null) {
                        // Rank not recognized
                        sender.sendMessage(ChatColor.RED + "Rank '" + args[2] + "' was not recognized.");
                        return true;
                    } else if (args.length == 3) {
                        // No number of claims specified; report current extra claims
                        sender.sendMessage("Current number of claims for " + rank.getColor() + rank.getName() + ChatColor.RESET + ": " + ChatColor.BOLD + rank.getExtraClaims());
                        return true;
                    } else {
                        // Number specified; change current extra claims
                        try {
                            rank.setExtraClaims(Integer.parseInt(args[3]));
                        } catch (NumberFormatException exception) {
                            sender.sendMessage(ChatColor.RED + "Please specify a valid number of extra claim chunks.");
                            return true;
                        }

                        sender.sendMessage("Set extra claim chunks for " + rank.getColor() + rank.getName() + ChatColor.RESET + ": " + ChatColor.BOLD + args[3]);
                        return true;
                    }
                }
            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank <create|delete|list|set|unset|color>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("player")) {
            if (args.length == 1) {
                // No arguments for /perms player
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " player <give|remove|info>");
                return true;
            } else if (args[1].equalsIgnoreCase("give")) {
                // Gives a player a specified rank.
                if (args.length < 4) {
                    // Either no arguments or only rank for /perms player give
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " player give <rank> <player>");
                    return true;
                } else {
                    // There are a given rank and player name.
                    // Get rank - may be null if unrecognized.
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    // Get player - may be null if unrecognized.
                    StringBuilder playerName = new StringBuilder(args[3]);
                    for (int i = 4; i < args.length; i++) {
                        playerName.append(" ").append(args[i]);
                    }

                    OfflinePlayer player = DatabaseLink.getSPSPlayer(playerName.toString());
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
                        if (SPSSpigot.perms().givePlayerRank(player.getUniqueId(), rank)) {
                            // The player did not have the rank before and we gave them it.
                            sender.sendMessage(ChatColor.GREEN + "Gave rank '" + rank.getName() + "' to player '" + DatabaseLink.getSPSName(player.getUniqueId()) + "'!");
                            return true;
                        } else {
                            // The player already had the rank.
                            sender.sendMessage(ChatColor.RED + "Player '" + DatabaseLink.getSPSName(player.getUniqueId()) + "' already has rank '" + rank.getName() + "'.");
                            return true;
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                // Removes a specified rank from a player.
                if (args.length < 4) {
                    // Either no arguments or only rank for /perms player remove
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " rank player <rank> <player>");
                    return true;
                } else {
                    // There are a given rank and player name.
                    // Get rank - may be null if unrecognized.
                    Rank rank = SPSSpigot.perms().getRank(args[2]);
                    // Get player - may be null if unrecognized (including offline).
                    StringBuilder playerName = new StringBuilder(args[3]);
                    for (int i = 4; i < args.length; i++) {
                        playerName.append(" ").append(args[i]);
                    }
                    OfflinePlayer player = DatabaseLink.getSPSPlayer(playerName.toString());
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
                        if (SPSSpigot.perms().removePlayerRank(player.getUniqueId(), rank)) {
                            // The player did have the rank before and we removed it.
                            sender.sendMessage(ChatColor.GREEN + "Removed rank '" + rank.getName() + "' from player '" + DatabaseLink.getSPSName(player.getUniqueId()) + "'!");
                            return true;
                        } else {
                            // The player did not already have the rank.
                            sender.sendMessage(ChatColor.RED + "Player '" + DatabaseLink.getSPSName(player.getUniqueId()) + "' already did not have '" + rank.getName() + "'.");
                            return true;
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("info")) {
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
                    OfflinePlayer player = DatabaseLink.getSPSPlayer(str.toString());
                    if (player == null) {
                        // Player not recognized
                        sender.sendMessage(ChatColor.RED + "Player '" + str + "' was not recognized.");
                        return true;
                    } else {
                        // Give them the info
                        List<String> items = new ArrayList<>();
                        // SPS info
                        if (DatabaseLink.isRegistered(player.getUniqueId()))
                            items.add(ChatColor.ITALIC + DatabaseLink.getSPSName(player.getUniqueId()));
                        else
                            items.add(ChatColor.ITALIC + "SPS profile unlinked");
                        // Player ranks
                        for (Rank rank : SPSSpigot.perms().getPlayerRanks(player.getUniqueId()))
                            items.add(rank.getColor() + rank.getName());
                        // Generate text
                        sender.sendMessage(CMD.buildListText(DatabaseLink.getSPSName(player.getUniqueId()), items));
                        return true;
                    }
                }
            } else {
                // args[1] is invalid
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + "player <give|remove|info>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            // Reloads perms from file.
            SPSSpigot.perms().loadFile();
            // Update perms for everybody online
            for (Player player : SPSSpigot.server().getOnlinePlayers())
                SPSSpigot.perms().loadPermissions(player);
            sender.sendMessage(ChatColor.GREEN + "Reloaded permissions!");
            return true;
        } else {
            // arg[0] is invalid
            return false;
        }
    }
}
