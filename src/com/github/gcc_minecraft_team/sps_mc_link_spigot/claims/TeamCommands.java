package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.database.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeamCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use these commands.");
            return true;
        }
        Player player = (Player) sender;
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(player.getWorld());
        if (worldGroup == null) {
            sender.sendMessage(ChatColor.RED + "This world is not in a world group, so teams cannot be made.");
            return true;
        } else if (args.length == 0) {
            // No arguments
            return false;
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 2) {
                // Either no args or too many args for /team create
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <name>");
                return true;
            } else if (worldGroup.getPlayerTeam(player.getUniqueId()) != null) {
                // Player is already on a team
                sender.sendMessage(ChatColor.RED + "You are already on a team. Please leave it before creating a new one.");
                return true;
            } else if (worldGroup.getTeam(args[1]) != null) {
                // Team with name already exists
                sender.sendMessage(ChatColor.RED + "A team already exists with the name '" + args[1] + "' please pick a different name.");
                return true;
            } else {
                // All checks passed.
                if (worldGroup.addTeam(new Team(worldGroup, args[1], player.getUniqueId()))) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully created new team '" + args[1] + "'!");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Something went wrong.");
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                // Either no args or too many args for /team join
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " join <name>");
                return true;
            } else if (worldGroup.getPlayerTeam(player.getUniqueId()) != null) {
                // Player is already on a team
                sender.sendMessage(ChatColor.RED + "You are already on a team. Please leave it before joining a different one.");
                return true;
            } else {
                Team team = worldGroup.getTeam(args[1]);
                if (team == null) {
                    // Team name not recognized.
                    sender.sendMessage(ChatColor.RED + "Team '" + args[1] + "' was not recognized.");
                    return true;
                } else if (team == worldGroup.getJoinRequest(player.getUniqueId())) {
                    // Player is already requesting to join this team. We don't want to let them use this to spam requests.
                    sender.sendMessage(ChatColor.RED + "You have already sent a join request to this team.");
                    return true;
                } else {
                    Team replace = worldGroup.newJoinRequest(player.getUniqueId(), team);
                    Player teamLeader = SPSSpigot.server().getPlayer(team.getLeader());
                    if (teamLeader != null) {
                        sender.sendMessage(ChatColor.GREEN + "Sent join request to " + team.getName() + ".");
                        teamLeader.sendMessage(ChatColor.GOLD + "Your team has a new join request!");
                        // TODO: Make the leader get tellraw to view requests
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "Sent join request to " + team.getName() + ". Their leader is not currently online.");
                    }
                    if (replace != null) {
                        sender.sendMessage(ChatColor.GREEN + "Canceled join request to " + replace.getName() + ".");
                    }
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            Team team = worldGroup.getPlayerTeam(player.getUniqueId());
            if (team == null) {
                // Player is not on a team
                sender.sendMessage(ChatColor.RED + "You need to on a team to kick players from the team.");
                return true;
            } else {
                if (team.getLeader().equals(player.getUniqueId())) {
                    if (args.length == 2) {
                        UUID kickPlayer = DatabaseLink.getSPSUUID(args[1]);
                        if (kickPlayer != null && team.removeMember(kickPlayer)) {
                            sender.sendMessage(ChatColor.BLUE + args[1] + " has been kicked from the team!");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "Couldn't remove player from team!");
                            return true;
                        }
                    } else {
                        sender.sendMessage("Please specify the player you want to kick.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You need to be a team leader to kick players from a team.");
                    return true;
                }
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            Team team = worldGroup.getPlayerTeam(player.getUniqueId());
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
        } else if (args[0].equals("list")) {
            if (args.length == 1) {
                // List all teams
                List<String> names = new ArrayList<>(worldGroup.getTeamNames());
                names.sort(String.CASE_INSENSITIVE_ORDER);
                sender.spigot().sendMessage(CMD.buildListTellraw("TEAMS", names, "/team list %s"));
                return true;
            } else if (args.length == 2) {
                Team team = worldGroup.getTeam(args[1]);
                if (team == null) {
                    // Team not recognized
                    sender.sendMessage(ChatColor.RED + "Team '" + args[1] + "' was not recognized.");
                    return true;
                } else {
                    // List info on the given team
                    sender.sendMessage(CMD.buildListText(team.getName(), team.getMemberNames()));
                    return true;
                }
            } else {
                // Invalid number of arguments
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " list [team]");
                return true;
            }
        } else if (args[0].equals("requests")) {
            Team team = worldGroup.getPlayerTeam(player.getUniqueId());
            if (args.length == 1) {
                // No arguments for /team requests
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " requests <list|accept|deny>");
                return true;
            } else if (team == null) {
                // Player is not on a team
                sender.sendMessage(ChatColor.RED + "You must be on a team to use team requests commands. If you want to join a team, use /team join");
                return true;
            } else if (args[1].equals("list")) {
                Set<UUID> requests = worldGroup.getTeamJoinRequests(team);
                ArrayList<String> names = new ArrayList<>();
                for (UUID req : requests)
                    names.add(DatabaseLink.getSPSName(req));
                sender.sendMessage(CMD.buildListText(team.getName() + " Join Requests", names));
                // TODO: Make this a tellraw for team leaders to be able to click to run accept/deny
                return true;
            } else if (args[1].equals("accept")) {
                if (team.getLeader().equals(player.getUniqueId())) {
                    if (args.length != 3) {
                        // No arguments or too many for /team requests accept
                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " requests accept <player>");
                        return true;
                    } else {
                        UUID uuid = DatabaseLink.getSPSUUID(args[2]);
                        if (uuid == null) {
                            // They are not registered with the name, but we also accept their actual UUID
                            try {
                                uuid = UUID.fromString(args[2]);
                            } catch (IllegalArgumentException e) {
                                // argument not recognized as a name nor as a UUID
                                sender.sendMessage(ChatColor.RED + "Player '" + args[2] + "' was not recognized.");
                                return true;
                            }
                        }
                        // If we got to this point, UUID is not null.
                        if (worldGroup.getJoinRequest(uuid) == team) {
                            worldGroup.fulfillJoinRequest(uuid);
                            sender.sendMessage(ChatColor.GREEN + "Successfully accepted join request!");
                            for (UUID member : team.getMembers()) {
                                Player onlineMember = SPSSpigot.server().getPlayer(member);
                                if (onlineMember != null)
                                    onlineMember.sendMessage(ChatColor.GOLD + DatabaseLink.getSPSName(uuid) + " joined the team!");
                            }
                            return true;
                        } else {
                            // The player does not have a join request to this team.
                            sender.sendMessage(ChatColor.RED + "Could not accept join request from " + DatabaseLink.getSPSName(uuid) + " because no request to your team was found.");
                            return true;
                        }
                    }
                } else {
                    // Player is not leader. They don't get to make decisions.
                    sender.sendMessage(ChatColor.RED + "You cannot accept or deny join requests if you are not the team leader.");
                    return true;
                }
            } else if (args[1].equals("deny")) {
                if (team.getLeader().equals(player.getUniqueId())) {
                    if (args.length != 3) {
                        // No arguments or too many for /team requests deny
                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " requests deny <player>");
                        return true;
                    } else {
                        UUID uuid = DatabaseLink.getSPSUUID(args[2]);
                        if (uuid == null) {
                            // They are not registered with the name, but we also accept their actual UUID
                            try {
                                uuid = UUID.fromString(args[2]);
                            } catch (IllegalArgumentException e) {
                                // argument not recognized as a name nor as a UUID
                                sender.sendMessage(ChatColor.RED + "Player '" + args[2] + "' was not recognized.");
                                return true;
                            }
                        }
                        // If we got to this point, UUID is not null.
                        if (worldGroup.getJoinRequest(uuid) == team) {
                            worldGroup.cancelJoinRequest(uuid);
                            sender.sendMessage(ChatColor.GREEN + "Successfully denied join request.");
                            Player onlinePlayer = SPSSpigot.server().getPlayer(uuid);
                            if (onlinePlayer != null)
                                onlinePlayer.sendMessage(ChatColor.RED + "Your join request to " + team.getName() + " was denied.");
                            return true;
                        } else {
                            // The player does not have a join request to this team.
                            sender.sendMessage(ChatColor.RED + "Could not deny join request from " + DatabaseLink.getSPSName(uuid) + " because no request to your team was found.");
                            return true;
                        }
                    }
                } else {
                    // Player is not leader. They don't get to make decisions.
                    sender.sendMessage(ChatColor.RED + "You cannot accept or deny join requests if you are not the team leader.");
                    return true;
                }
            } else {
                // invalid argument
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " requests <list|accept|deny>");
                return true;
            }
        } else {
            // args[0] is invalid
            return false;
        }
    }
}
