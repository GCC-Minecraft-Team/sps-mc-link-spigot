package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class MapEvents implements Listener {

    @EventHandler
    public void onMap(MapInitializeEvent e) {
        if (MapRegistry.maps.containsKey(e.getMap().getId())) {
            MapRegistry.CustomMap mapSerial = MapRegistry.maps.get(e.getMap().getId());
            if (mapSerial instanceof MapRegistry.PlayerMap) {
                e.getMap().getRenderers().clear();
                e.getMap().addRenderer(new PlayerMapRenderer((MapRegistry.PlayerMap) mapSerial));
            } else if (mapSerial instanceof MapRegistry.ImageMap) {
                e.getMap().getRenderers().clear();
                e.getMap().addRenderer(new ImageMapRenderer((MapRegistry.ImageMap) mapSerial));
            } else if (mapSerial instanceof MapRegistry.ClaimMap) {
                e.getMap().getRenderers().clear();
                e.getMap().addRenderer(new ClaimMapRenderer());
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
