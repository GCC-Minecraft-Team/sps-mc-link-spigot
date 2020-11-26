package com.github.gcc_minecraft_team.sps_mc_link_spigot.claims;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.CMD;
import com.github.gcc_minecraft_team.sps_mc_link_spigot.SPSSpigot;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.*;

public class ClaimEvents implements Listener {

    /**
     * A {@link List} of all the {@link Material}s that players are allowed to interact with in others' claims.
     */
    private static final List<Material> interactExceptions = Arrays.asList(
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
            // Signs
            Material.ACACIA_SIGN, Material.OAK_SIGN, Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN, Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN, Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN, Material.DARK_OAK_SIGN,
            Material.DARK_OAK_WALL_SIGN, Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN, Material.OAK_WALL_SIGN, Material.WARPED_SIGN,
            Material.WARPED_WALL_SIGN,
            // Ender chest
            Material.ENDER_CHEST,
            // Lectern (taking is blocked)
            Material.LECTERN);

    private static final Map<UUID, Integer> prevSector = new HashMap<>();

    /*
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        // Handle board updates here when the player rotates enough or changes chunks.
        // Nathan: This doesn't work well on the server, it only updates like 30% of the time

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
    }
    */

    // ========[PLAYER BLOCK INTERACTION]========

    /*
    Only disable vehicles in claims not spawn
     */
    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() != null) {
            Chunk chunk = event.getAttacker().getLocation().getChunk();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
            if (worldGroup != null && !worldGroup.hasOverride(event.getAttacker().getUniqueId())) {
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
        if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
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
            if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
                if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld()))
                    event.setCancelled(true);
                else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Chunk chunk = event.getLectern().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld()))
                event.setCancelled(true);
            else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(chunk.getWorld());
        if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
            if (worldGroup.isInSpawn(event.getPlayer().getLocation()) && worldGroup.isClaimable(event.getPlayer().getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(event.getPlayer().getUniqueId(), chunk, true))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getPlayer().getWorld());
        if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
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
                if (worldGroup != null && !worldGroup.hasOverride(event.getPlayer().getUniqueId())) {
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

    // ========[EXPLOSIONS]========

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
        if (worldGroup != null) {
            UUID owner = worldGroup.getChunkOwner(event.getBlock().getChunk());
            if (owner == null || !worldGroup.hasOverride(owner)) {
                for (int i = event.blockList().size() - 1; i >= 0; i--) {
                    Block block = event.blockList().get(i);
                    if (worldGroup.isInSpawn(block.getLocation()) && worldGroup.isClaimable(block.getWorld())) {
                        event.blockList().remove(i);
                    } else if (!worldGroup.canModifyChunk(owner, block.getChunk(), false)) {
                        event.blockList().remove(i);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        // Stop TNT explosions from breaking claimed blocks
        if (!event.getEntityType().isAlive()) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getEntity().getWorld());
            if (worldGroup != null) {
                Location origin = event.getEntity().getOrigin();
                UUID owner;
                if (tnt.hasMetadata("owner")) {
                    MetadataValue metaValue = CMD.getPluginMetadata(tnt, "owner");
                    if (metaValue == null || metaValue.asString().equals("null"))
                        owner = null;
                    else
                        owner = UUID.fromString(metaValue.asString());
                } else {
                    if (origin == null)
                        owner = null;
                    else
                        owner = worldGroup.getChunkOwner(origin.getChunk());
                }
                if (owner == null || !worldGroup.hasOverride(owner)) {
                    for (int i = event.blockList().size() - 1; i >= 0; i--) {
                        Block block = event.blockList().get(i);
                        if (worldGroup.isInSpawn(block.getLocation()) && worldGroup.isClaimable(block.getWorld())) {
                            event.blockList().remove(i);
                        } else if (!worldGroup.canModifyChunk(owner, block.getChunk(), false)) {
                            event.blockList().remove(i);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTNTPrime(TNTPrimeEvent event) {
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
        if (worldGroup != null && event.getPrimerEntity() != null) {
            Chunk chunk = event.getBlock().getChunk();
            UUID player;
            boolean isPlayer = false;
            if (event.getPrimerEntity() instanceof Player) {
                player = event.getPrimerEntity().getUniqueId();
                isPlayer = true;
            } else if (event.getPrimerEntity() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getPrimerEntity();
                if (projectile.getShooter() instanceof Player) {
                    player = ((Player) projectile.getShooter()).getUniqueId();
                    isPlayer = true;
                } else if (projectile.getShooter() instanceof BlockProjectileSource) {
                    player = worldGroup.getChunkOwner(((BlockProjectileSource) projectile.getShooter()).getBlock().getChunk());
                } else if (projectile.getShooter() instanceof Entity) {
                    // This is really a matter of choice. Do we exclude other entities like a skeleton with a flame bow?
                    player = worldGroup.getChunkOwner(((Entity) projectile.getShooter()).getChunk());
                } else {
                    player = null;
                }
            } else {
                // TODO: Is is possible to also block tnt explosion spread? This is currently a loophole to trigger existing TNT.
                player = null;
            }
            if (worldGroup.isInSpawn(event.getBlock().getLocation()) && worldGroup.isClaimable(event.getBlock().getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(player, event.getBlock().getChunk(), isPlayer)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        // Override TNT spawns to have the origin be the dispenser block.
        if (event.getItem().getType().equals(Material.TNT)) {
            event.setCancelled(true);
            WorldGroup worldGroup = SPSSpigot.getWorldGroup(event.getBlock().getWorld());
            if (worldGroup != null) {
                UUID owner = worldGroup.getChunkOwner(event.getBlock().getChunk());
                String ownerStr;
                if (owner == null)
                    ownerStr = "null";
                else
                    ownerStr = owner.toString();
                Location facing = event.getBlock().getRelative(((Directional) event.getBlock().getBlockData()).getFacing()).getLocation();
                TNTPrimed tnt = (TNTPrimed) event.getBlock().getWorld().spawnEntity(facing.toCenterLocation(), EntityType.PRIMED_TNT);
                tnt.setMetadata("owner", new FixedMetadataValue(SPSSpigot.plugin(), ownerStr));
            }
        }
    }

    // ========[ENTITIES]========

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        final Player p = e.getEntity();
        p.sendTitle(ChatColor.DARK_RED + "You Died", "Auto Respawning", 10, 60, 10);
        Bukkit.getScheduler().scheduleSyncDelayedTask(SPSSpigot.plugin(), () -> p.spigot().respawn(), 2);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Player attacker;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            } else {
                return;
            }
        } else {
            return;
        }
        // At this point, target and attacker are set.
        WorldGroup worldGroup = SPSSpigot.getWorldGroup(target.getWorld());
        if (worldGroup != null && !worldGroup.hasOverride(attacker.getUniqueId())) {
            if (worldGroup.isEntityInSpawn(target) && worldGroup.isClaimable(target.getWorld())) {
                event.setCancelled(true);
            } else if (!worldGroup.canModifyChunk(attacker.getUniqueId(), target.getLocation().getChunk(), true)) {
                if (target instanceof Mob) {
                    Mob mob = (Mob) target;
                    if (mob instanceof Animals || mob instanceof WaterMob || mob instanceof NPC) {
                        // Allow defense against actively hostile animals like aggro wolves or polar bears
                        // Otherwise, cancel:
                        if (mob.getTarget() == null) {
                            event.setCancelled(true);
                        }
                    }
                } else {
                    // It's not a mob at all. It's a tile entity, a mine cart, or something like that.
                    event.setCancelled(true);
                }
            }
        }
    }
}
