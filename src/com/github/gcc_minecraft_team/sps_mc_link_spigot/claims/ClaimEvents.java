package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.*;

public class ClaimEvents implements Listener {

    /**
     * A {@link List} of all the {@link Material}s that players are allowed to interact with in others' claims.
     */
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

    private static final Map<UUID, Integer> prevSector = new HashMap<>();

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        // Handle board updates here when the player rotates enough or changes chunks.
        // Nathan: This doesn't work well on the server, it only updates like 30% of the time
        /*
        Location from = event.getFrom();
        Location to = event.getTo();
        UUID player = event.getPlayer().getUniqueId();
        FastBoard board = SPSSpigot.plugin().boards.get(player);
        if (to != null && board != null && !board.isDeleted()) {
            int toSector = CMD.sector(4, 45, to.getYaw());
            if (!from.getChunk().equals(to.getChunk()) || !prevSector.containsKey(player) || toSector != prevSector.get(player)) {
                // The player has either changed chunks or rotated towards another cardinal direction.
                SPSSpigot.plugin().updateBoard(board);
            }
            prevSector.put(player, toSector);
        }
         */
    }

    // ========[PLAYER BLOCK INTERACTION]========

    /*
    Only disable vehicles in claims not spawn
     */
    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() != null) {
            Chunk chunk = event.getAttacker().getLocation().getChunk();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
            if (worldGroup != null) {
                if (!worldGroup.canModifyChunk(event.getAttacker().getUniqueId(), chunk, true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && !interactExceptions.contains(event.getClickedBlock().getType())) {
            Chunk chunk = event.getClickedBlock().getChunk();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
            if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getPlayer().getWorld());
        if (worldGroup != null) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation())) {
                // We don't need to prevent put items into the empty item frame (that's out of scope of this plugin)
                if (event.getRightClicked() instanceof ItemFrame && !((ItemFrame) event.getRightClicked()).getItem().getType().equals(Material.AIR)) {
                    event.setCancelled(true);
                }
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), event.getRightClicked().getLocation().getChunk(), true)) {
                // If a player is trying to interact with an entity in another player's claim.

                // Get the item the player is using to interact with the entity:
                Material interactItem = event.getPlayer().getInventory().getItem(event.getHand()).getType();

                if (interactItem.equals(Material.NAME_TAG)) {
                    // Don't let players rename in other people's claims.
                    event.setCancelled(true);
                } else if (interactItem.equals(Material.SHEARS)) {
                    // Don't let players shear animals in other people's claims.
                    event.setCancelled(true);
                } else if (event.getRightClicked() instanceof ArmorStand || event.getRightClicked() instanceof ItemFrame) {
                    // Don't let player interact with armor stands or item frames and steal stuff.
                    event.setCancelled(true);
                } else if (event.getRightClicked() instanceof StorageMinecart || event.getRightClicked() instanceof HopperMinecart || event.getRightClicked() instanceof PoweredMinecart) {
                    // Don't let player steal from minecarts with inventories.
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() != null) {
            if (!event.getPlayer().isOp()) {
                Chunk toChunk = event.getBlock().getChunk();
                WorldGroup worldGroup = SPSSpigot.getWorldGroup(toChunk.getWorld());
                if (worldGroup != null) {
                    if (worldGroup.isInSpawn(event.getBlock().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld())) {
                        event.setCancelled(true);
                    } else if (event.getPlayer() != null) {
                        if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), toChunk, true))
                            event.setCancelled(true);
                    } else if (event.getIgnitingBlock() != null) {
                        // It was done by a block.
                        // Cancel if the source block's owner is not allowed to modify the fire block.
                        if (!worldGroup.canModifyChunk(worldGroup.getChunkOwner(event.getIgnitingBlock().getChunk()), toChunk, false))
                            event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Chunk chunk = event.getLectern().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null && !worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
            event.setCancelled(true);
    }

    // ========[BLOCKS]========

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
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
                if (!worldGroup.canModifyChunk(piston, chunk, false)) {
                    event.setCancelled(true);
                    return;
                }
            }
            for (Chunk chunk : moveTarget) {
                if (!worldGroup.canModifyChunk(piston, chunk, false)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
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
                if (!worldGroup.canModifyChunk(piston, chunk, false)) {
                    event.setCancelled(true);
                    return;
                }
            }
            for (Chunk chunk : moveTarget) {
                if (!worldGroup.canModifyChunk(piston, chunk, false)) {
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
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
            if (worldGroup != null && !worldGroup.canModifyChunk(worldGroup.getChunkOwner(event.getSource().getChunk()), event.getBlock().getChunk(), false)) {
                event.setCancelled(true);
            }
        }
    }

    // ========[ENTITIES]========

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        // Stop TNT explosions from breaking claimed blocks
        // Nathan: Temporarily removing this as people should be able to use TNT in their own claims
        /*
        if (!event.getEntityType().isAlive()) {
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getEntity().getWorld());
            if (worldGroup != null) {
                for (int i = event.blockList().size() - 1; i >= 0; i--) {
                    if (worldGroup.getChunkOwner(event.blockList().get(i).getChunk()) != null) {
                        event.blockList().remove(i);
                    }
                }
            }
        }
         */
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        final Player p = e.getEntity();
        p.sendTitle(ChatColor.DARK_RED + "You Died", "Auto Respawning", 10, 60, 10);
        Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> p.spigot().respawn(), 2);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(target.getWorld());
        if (worldGroup != null) {
            if (worldGroup.isEntityInSpawn(event.getDamager()) && worldGroup.isClaimable(event.getEntity().getWorld())) {
                event.setCancelled(true);
            } else if (event.getDamager() instanceof Player && !worldGroup.canModifyChunk(event.getDamager().getUniqueId(), target.getLocation().getChunk(), true)) {
                if (event.getEntity() instanceof Mob) {
                    Mob mob = (Mob) event.getEntity();
                    if (mob instanceof Animals || mob instanceof WaterMob || mob instanceof NPC) {
                        // Allow defense against actively hostile animals like aggro wolves or polar bears
                        // Otherwise, cancel:
                        if (mob.getTarget() == null) {
                            event.setCancelled(true);
                        }
                    }
                } else {
                    // It's not a mob at all. It's a tile entity or something like that.
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        Projectile p = event.getEntity();
        if (p.getShooter() instanceof Player) {
            Player player = (Player) p.getShooter();
            Chunk chunk = event.getLocation().getChunk();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
            if (worldGroup != null) {
                if (worldGroup.isInSpawn(player.getLocation()) && worldGroup.isClaimable(player.getWorld())) {
                    event.setCancelled(true);
                } else if (!worldGroup.canModifyChunk(player.getUniqueId(), chunk, true))
                    event.setCancelled(true);
            }
        }
    }
}
