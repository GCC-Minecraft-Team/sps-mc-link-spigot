package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

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


    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        ClaimHandler worldGroup = SPSSpigot.claims(event.getPlayer().getWorld());
        if (worldGroup != null && worldGroup.isInSpawn(event.getPlayer().getLocation())) {
            if (event.getRightClicked() instanceof ItemFrame && !((ItemFrame) event.getRightClicked()).getItem().getType().equals(Material.AIR)) { // We don't need to prevent put items into the empty item frame (that's out of scope of this plugin)
                event.setCancelled(true);
            }
        }
    }

    // Claim modification
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && !interactExceptions.contains(event.getClickedBlock().getType())) {
            Chunk chunk = event.getClickedBlock().getChunk();
            ClaimHandler worldGroup = SPSSpigot.claims(chunk.getWorld());
            if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        ClaimHandler worldGroup = SPSSpigot.claims(event.getLocation().getWorld());
        if (!event.getEntity().getType().equals(EntityType.DROPPED_ITEM)) {
            if (worldGroup != null && worldGroup.isEntityInSpawn(event.getEntity()) && event.getEntity().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        ClaimHandler worldGroup = SPSSpigot.claims(chunk.getWorld());
        if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        ClaimHandler worldGroup = SPSSpigot.claims(chunk.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && event.getPlayer().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        ClaimHandler worldGroup = SPSSpigot.claims(target.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isEntityInSpawn(event.getDamager()) && event.getEntity().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                event.setCancelled(true);
            } else if (event.getDamager() instanceof Player && !worldGroup.canModifyChunk(event.getDamager().getUniqueId(), target.getLocation().getChunk())) {
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
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Chunk toChunk = event.getBlock().getChunk();
        ClaimHandler worldGroup = SPSSpigot.claims(toChunk.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && event.getPlayer().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                event.setCancelled(true);
            } else if (event.getPlayer() != null) {
                if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), toChunk))
                    event.setCancelled(true);
            } else if (event.getIgnitingEntity() != null) {
                // It was done by a non-player entity.
                // I guess let it happen.
            } else if (event.getIgnitingBlock() != null) {
                // It was done by a block.
                // Cancel if the source block's owner is not allowed to modify the fire block.
                if (!worldGroup.canModifyChunk(worldGroup.getChunkOwner(event.getIgnitingBlock().getChunk()), toChunk))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Chunk chunk = event.getLectern().getChunk();
        ClaimHandler worldGroup = SPSSpigot.claims(chunk.getWorld());
        if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        ClaimHandler worldGroup = SPSSpigot.claims(event.getBlock().getWorld());
        if (worldGroup != null) {
            UUID piston = worldGroup.getChunkOwner(event.getBlock().getChunk());
            Set<Chunk> moveCurrent = new HashSet<>();
            for (Block block : event.getBlocks()) {
                moveCurrent.add(block.getChunk());
            }
            Set<Chunk> moveTarget = new HashSet<>();
            for (Block block : event.getBlocks()) {
                moveTarget.add(block.getRelative(event.getDirection()).getChunk());
            }
            for (Chunk chunk : moveCurrent) {
                if (!worldGroup.canModifyChunk(piston, chunk)) {
                    event.setCancelled(true);
                    return;
                }
            }
            for (Chunk chunk : moveTarget) {
                if (!worldGroup.canModifyChunk(piston, chunk)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        ClaimHandler worldGroup = SPSSpigot.claims(event.getBlock().getWorld());
        if (worldGroup != null) {
            UUID piston = worldGroup.getChunkOwner(event.getBlock().getChunk());
            Set<Chunk> moveCurrent = new HashSet<>();
            for (Block block : event.getBlocks()) {
                moveCurrent.add(block.getChunk());
            }
            Set<Chunk> moveTarget = new HashSet<>();
            for (Block block : event.getBlocks()) {
                moveTarget.add(block.getRelative(event.getDirection()).getChunk());
            }
            for (Chunk chunk : moveCurrent) {
                if (!worldGroup.canModifyChunk(piston, chunk)) {
                    event.setCancelled(true);
                    return;
                }
            }
            for (Chunk chunk : moveTarget) {
                if (!worldGroup.canModifyChunk(piston, chunk)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent event) {
        // Only CLAIMS (and you) can prevent forest fires!
        if (event.getBlock().getType().equals(Material.FIRE)) {
            ClaimHandler worldGroup = SPSSpigot.claims(event.getBlock().getWorld());
            if (worldGroup != null && !worldGroup.canModifyChunk(worldGroup.getChunkOwner(event.getSource().getChunk()), event.getBlock().getChunk())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        // Stop TNT explosions from breaking claimed blocks
        if (!event.getEntityType().isAlive()) {
            ClaimHandler worldGroup = SPSSpigot.claims(event.getEntity().getWorld());
            if (worldGroup != null) {
                for (int i = event.blockList().size() - 1; i >= 0; i--) {
                    if (worldGroup.getChunkOwner(event.blockList().get(i).getChunk()) != null) {
                        event.blockList().remove(i);
                    }
                }
            }
        }
    }
}
