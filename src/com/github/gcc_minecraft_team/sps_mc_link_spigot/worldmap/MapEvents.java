package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class MapEvents implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof ItemFrame) {
                initMap(((ItemFrame)entity).getItem());
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        initMap(item);
    }

    static public void initMap(ItemStack item) {
        if (item != null && item.getType() == Material.FILLED_MAP) {
            initMap(MapRegistry.getMapIdFromItemStack(item));
        }
    }

    static public void initMap(int id) {
        initMap(Bukkit.getServer().getMap(id));
    }

    static public void initMap(MapView map) {
        if (map == null || map.isVirtual()) {
            return;
        }

        if (MapRegistry.maps.containsKey(map.getId())) {
            MapRegistry.CustomMap mapSerial = MapRegistry.maps.get(map.getId());
            if (mapSerial instanceof MapRegistry.PlayerMap) {
                map.getRenderers().clear();
                map.addRenderer(new PlayerMapRenderer((MapRegistry.PlayerMap) mapSerial));
            } else if (mapSerial instanceof MapRegistry.ImageMap) {
                map.getRenderers().clear();
                map.addRenderer(new ImageMapRenderer((MapRegistry.ImageMap) mapSerial));
            } else if (mapSerial instanceof MapRegistry.ClaimMap) {
                map.getRenderers().clear();
                map.addRenderer(new ClaimMapRenderer());
            }
        }
    }

    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent event) {
        ItemStack stack = event.getItemDrop().getItemStack();
        // Faction map scaling
        if (MapRegistry.isClaimMap(stack)) {
            if (event.getPlayer().isSneaking()) {
                MapView view = ((MapMeta) stack.getItemMeta()).getMapView();
                if (view.getScale().equals(MapView.Scale.CLOSEST))
                    view.setScale(MapView.Scale.CLOSE);
                else if (view.getScale().equals(MapView.Scale.CLOSE))
                    view.setScale(MapView.Scale.NORMAL);
                else if (view.getScale().equals(MapView.Scale.NORMAL))
                    view.setScale(MapView.Scale.FAR);
                else if (view.getScale().equals(MapView.Scale.FAR))
                    view.setScale(MapView.Scale.FARTHEST);
                else if (view.getScale().equals(MapView.Scale.FARTHEST))
                    view.setScale(MapView.Scale.CLOSEST);
                event.setCancelled(true);
            }
        }
    }
}
