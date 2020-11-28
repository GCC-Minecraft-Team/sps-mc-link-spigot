package com.github.gcc_minecraft_team.sps_mc_link_spigot.permissions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

public class PermissionsEvents implements Listener {

    // ========[CLICKS]========

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission("spsmc.basic.interact"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().hasPermission("spsmc.basic.interactentity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamageEvent(BlockDamageEvent event) {
        if (!event.getPlayer().hasPermission("spsmc.basic.attackblock"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && !event.getDamager().hasPermission("spsmc.basic.attackentity"))
            event.setCancelled(true);
    }

    // ========[MOVING]========

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        // Stop movement but not rotation
        if (!event.getPlayer().hasPermission("spsmc.basic.move")) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())
                event.setCancelled(true);
        }
    }

    // ========[ITEMS]========

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().hasPermission("spsmc.basic.itempickup"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDropItemEvent(EntityDropItemEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().hasPermission("spsmc.basic.itemdrop"))
            event.setCancelled(true);
    }

    // ========[CHAT]========

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("spsmc.basic.chat"))
            event.setCancelled(true);
    }
}
