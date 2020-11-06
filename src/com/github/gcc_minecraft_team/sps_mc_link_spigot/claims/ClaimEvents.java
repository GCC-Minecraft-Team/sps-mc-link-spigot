package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import java.util.*;

public class ClaimEvents implements Listener {

    List<Material> interactExceptions = Arrays.asList(
            // Doors (not iron)
            Material.OAK_DOOR, Material.BIRCH_DOOR, Material.SPRUCE_DOOR, Material.JUNGLE_DOOR,
            Material.DARK_OAK_DOOR, Material.ACACIA_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR,
            // Trapdoors (not iron)
            Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR,
            // Fence gates
            Material.OAK_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE,
            // Crafting tables without inventories
            Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE, Material.STONECUTTER,
            Material.CARTOGRAPHY_TABLE, Material.LOOM, Material.SMITHING_TABLE,
            // Ender chest
            Material.ENDER_CHEST);

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        if (!to.equals(from)) {
            UUID fromOwner = SPSSpigot.claims().getChunkOwner(from);
            UUID toOwner = SPSSpigot.claims().getChunkOwner(to);
            if (toOwner == null) {
                if (fromOwner != null)
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder().color(ChatColor.DARK_GREEN).append("Entering Wilderness.").create());
            } else {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder("Entering " + DatabaseLink.getSPSName(toOwner) + "'s Claim.").create());
            }
        }
    }

    // Claim modification
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Exceptions:
        if (event.hasBlock() && !interactExceptions.contains(event.getClickedBlock().getType())) {
            Chunk chunk = event.getClickedBlock().getChunk();
            if (!SPSSpigot.claims().canModifyChunk(event.getPlayer().getUniqueId(), chunk))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        if (!SPSSpigot.claims().canModifyChunk(event.getPlayer().getUniqueId(), chunk))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        if (!SPSSpigot.claims().canModifyChunk(event.getPlayer().getUniqueId(), chunk))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (event.getDamager() instanceof Player && !SPSSpigot.claims().canModifyChunk(event.getDamager().getUniqueId(), target.getLocation().getChunk())) {
            if (event.getEntity() instanceof Mob) {
                Mob mob = (Mob) event.getEntity();
                if (mob instanceof Animals || mob instanceof WaterMob || mob instanceof NPC) {
                    // Allow defense against actively hostile animals like aggro wolves or polar bears
                    // Otherwise, cancel:
                    if (mob.getTarget() == null) {
                        event.setCancelled(true);
                    }
                }
            } else if (!(event.getEntity() instanceof Projectile)) {
                // It's not a mob at all. It's a tile entity or something like that.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Chunk toChunk = event.getBlock().getChunk();
        if (event.getPlayer() != null) {
            if (!SPSSpigot.claims().canModifyChunk(event.getPlayer().getUniqueId(), toChunk))
                event.setCancelled(true);
        } else if (event.getIgnitingEntity() != null) {
            // It was done by a non-player entity.
            // I guess let it happen.
        } else if (event.getIgnitingBlock() != null) {
            // It was done by a block.
            // Cancel if the source block's owner is not allowed to modify the fire block.
            if (!SPSSpigot.claims().canModifyChunk(SPSSpigot.claims().getChunkOwner(event.getIgnitingBlock().getChunk()), toChunk))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Chunk chunk = event.getLectern().getChunk();
        if (!SPSSpigot.claims().canModifyChunk(event.getPlayer().getUniqueId(), chunk))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        UUID piston = SPSSpigot.claims().getChunkOwner(event.getBlock().getChunk());
        Set<Chunk> moveCurrent = new HashSet<>();
        for (Block block : event.getBlocks()) {
            moveCurrent.add(block.getChunk());
        }
        Set<Chunk> moveTarget = new HashSet<>();
        for (Block block : event.getBlocks()) {
            moveTarget.add(block.getRelative(event.getDirection()).getChunk());
        }
        for (Chunk chunk : moveCurrent) {
            if (!SPSSpigot.claims().canModifyChunk(piston, chunk)) {
                event.setCancelled(true);
                return;
            }
        }
        for (Chunk chunk : moveTarget) {
            if (!SPSSpigot.claims().canModifyChunk(piston, chunk)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        UUID piston = SPSSpigot.claims().getChunkOwner(event.getBlock().getChunk());
        Set<Chunk> moveCurrent = new HashSet<>();
        for (Block block : event.getBlocks()) {
            moveCurrent.add(block.getChunk());
        }
        Set<Chunk> moveTarget = new HashSet<>();
        for (Block block : event.getBlocks()) {
            moveTarget.add(block.getRelative(event.getDirection()).getChunk());
        }
        for (Chunk chunk : moveCurrent) {
            if (!SPSSpigot.claims().canModifyChunk(piston, chunk)) {
                event.setCancelled(true);
                return;
            }
        }
        for (Chunk chunk : moveTarget) {
            if (!SPSSpigot.claims().canModifyChunk(piston, chunk)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent event) {
        // Only CLAIMS (and you) can prevent forest fires!
        if (event.getBlock().getType().equals(Material.FIRE)) {
            if (!SPSSpigot.claims().canModifyChunk(SPSSpigot.claims().getChunkOwner(event.getSource().getChunk()), event.getBlock().getChunk())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        // Stop TNT explosions from breaking claimed blocks
        if (!event.getEntityType().isAlive()) {
            for (int i = event.blockList().size() - 1; i >= 0; i--) {
                if (SPSSpigot.claims().getChunkOwner(event.blockList().get(i).getChunk()) != null) {
                    event.blockList().remove(i);
                }
            }
        }
    }
}
