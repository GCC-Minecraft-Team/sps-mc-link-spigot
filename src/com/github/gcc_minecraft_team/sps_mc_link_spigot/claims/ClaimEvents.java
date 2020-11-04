package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.DatabaseLink;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import java.util.UUID;

public class ClaimEvents implements Listener {

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        if (!to.equals(from)) {
            UUID fromOwner = SPSSpigot.claims().getChunkOwner(from);
            UUID toOwner = SPSSpigot.claims().getChunkOwner(to);
            if (toOwner == null) {
                if (fromOwner != null)
                    event.getPlayer().sendTitle("", ChatColor.DARK_GREEN.toString() + "Entered wilderness", 8, 60, 12);
            } else {
                event.getPlayer().sendTitle("", "Entered " + DatabaseLink.getSPSName(toOwner) + "'s claim", 8, 60, 12);
            }
        }
    }

    // Claim modification
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
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
}
