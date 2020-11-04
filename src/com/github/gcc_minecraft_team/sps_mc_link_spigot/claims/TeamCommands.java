package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamCommands implements CommandExecutor {

    /**
     * Builds the text for a list to be sent in chat.
     * @param title The title of the list. May safely include {@link ChatColor}s.
     * @param items A {@link List} of the items to include.
     * @return A multi-line text representation of the list.
     */
    public static String buildListText(String title, List<String> items) {
        StringBuilder str = new StringBuilder(ChatColor.BOLD + "====[" + title.strip() + ChatColor.RESET + ChatColor.BOLD + "]====\n");
        for (String item : items) {
            str.append(ChatColor.RESET + item.strip() + "\n");
        }
        return str.toString();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equalsIgnoreCase("create")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 2) {
                    // Either no args or too many args for /team create
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <name>");
                    return true;
                } else if (SPSSpigot.claims().getPlayerTeam(player.getUniqueId()) != null) {
                    // Player is already on a team
                    sender.sendMessage(ChatColor.RED + "You are already on a team. Please leave it before creating a new one.");
                    return true;
                } else if (SPSSpigot.claims().getTeam(args[1]) != null) {
                    // Team with name already exists
                    sender.sendMessage(ChatColor.RED + "A team already exists with the name '" + args[1] + "' please pick a different name.");
                    return true;
                } else {
                    // All checks passed.
                    if (SPSSpigot.claims().addTeam(new Team(args[1], player.getUniqueId()))) {
                        sender.sendMessage(ChatColor.GREEN + "Successfully created new team '" + args[1] + "'!");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Something went wrong.");
                        return true;
                    }
                }
            } else {
                // Not a player
                sender.sendMessage(ChatColor.RED + "Only players may create teams.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("join")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 2) {
                    // Either no args or too many args for /team join
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " join <name>");
                    return true;
                } else if (SPSSpigot.claims().getPlayerTeam(player.getUniqueId()) != null) {
                    // Player is already on a team
                    sender.sendMessage(ChatColor.RED + "You are already on a team. Please leave it before joining a different one.");
                    return true;
                } else {
                    Team team = SPSSpigot.claims().getTeam(args[1]);
                    if (team == null) {
                        // Team name not recognized.
                        sender.sendMessage(ChatColor.RED + "Team '" + args[1] + "' was not recognized.");
                        return true;
                    } else {
                        // TODO: Send a join request
                        sender.sendMessage("NOT IMPLEMENTED");
                        return true;
                    }
                }
            } else {
                // Not a player
                sender.sendMessage(ChatColor.RED + "Only players may create teams.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Team team = SPSSpigot.claims().getPlayerTeam(player.getUniqueId());
                if (team == null) {
                    // Player is already not on a team
                    sender.sendMessage(ChatColor.RED + "You cannot leave a team if you are not on one.");
                    return true;
                } else if (team.removeMember(player)) {
                    // Left the team
                    sender.sendMessage(ChatColor.GREEN + "Successfully left team '" + team.getName() + "'!");
                    // Inform all online team members
                    for (UUID uuid : team.getMembers()) {
                        if (!uuid.equals(player.getUniqueId())) {
                            Player teamMember = SPSSpigot.server().getPlayer(uuid);
                            if (teamMember != null)
                                teamMember.sendMessage(DatabaseLink.getSPSName(player.getUniqueId()) + " left the team.");
                        }
                    }
                    return true;
                } else {
                    // The player is the leader and thus cannot leave.
                    sender.sendMessage(ChatColor.RED + "You cannot leave if you are the leader and are not the last member of the team.");
                    return true;
                }
            } else {
                // Not a player
                sender.sendMessage(ChatColor.RED + "Only players may create teams.");
                return true;
            }
        } else if (args[0].equals("list")) {
            if (args.length == 1) {
                // List all teams
                List<String> names = new ArrayList<>(SPSSpigot.claims().getTeamNames());
                names.sort(String.CASE_INSENSITIVE_ORDER);
                sender.sendMessage(buildListText("TEAMS", names));
                return true;
            } else if (args.length == 2) {
                Team team = SPSSpigot.claims().getTeam(args[1]);
                if (team == null) {
                    // Team not recognized
                    sender.sendMessage(ChatColor.RED + "Team '" + args[1] + "' was not recognized.");
                    return true;
                } else {
                    // List info on the given team
                    sender.sendMessage(buildListText(team.getName(), team.getMemberNames()));
                    return true;
                }
            } else {
                // Invalid number of arguments
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " list [team]");
                return true;
            }
        } else {
            // args[0] is invalid
            return false;
        }
    }
}
