package com.github.gcc_minecraft_team.sps_mc_link_spigot.worldmap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.io.IOException;

public class MapEvents implements Listener {

    private int curMaxMaps = 0;

    @EventHandler
    public void onMap(MapInitializeEvent e) {

        if (e.getMap().getId() == 0) {
            drawMap(e, -1, -1);
        } else if (e.getMap().getId() == 1) {
            drawMap(e, 0, -1);
        } else if (e.getMap().getId() == 2) {
            drawMap(e, -1, 0);
        } else if (e.getMap().getId() == 3) {
            drawMap(e, 0, 0);
        } else if (e.getMap().getId() > 3) {
            // spawn signboards
            // TODO: support adding and removing images and stuff
            assignMaps(e, "rules.png", 4);
            assignMaps(e, "info.png", 8);
            assignMaps(e, "staff.png", 12);
            if (e.getMap().getId() == 16) {
                drawSignboard(e, 0, 0, "tf.jpg");
            }
        }
    }

    private void assignMaps(MapInitializeEvent e, String filename, int curMaxMaps) {
        if (e.getMap().getId() == curMaxMaps) {
            drawSignboard(e, 0, 0, filename);
        }
        if (e.getMap().getId() == curMaxMaps + 1) {
            drawSignboard(e, 1, 0, filename);
        }
        if (e.getMap().getId() == curMaxMaps + 2) {
            drawSignboard(e, 0, 1, filename);
        }
        if (e.getMap().getId() == curMaxMaps + 3) {
            drawSignboard(e, 1, 1, filename);
        }
    }

    private void drawSignboard(MapInitializeEvent e, int offsetX, int offsetZ, String filename) {
        for (MapRenderer r : e.getMap().getRenderers()) {
            e.getMap().removeRenderer(r);
        }

        MapView map = e.getMap();

        SignboardRenderer sr = new SignboardRenderer();
        sr.setOffest(offsetX * 128, offsetZ * 128);
        try {
            sr.loadImage(filename);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        map.addRenderer(sr);
    }

    private void drawMap(MapInitializeEvent e, int offsetX, int offsetZ) {
        for (MapRenderer r : e.getMap().getRenderers()) {
            e.getMap().removeRenderer(r);
        }

        MapView map = e.getMap();

        PlayerRenderer pr = new PlayerRenderer();
        pr.setOffest(offsetX * 2048, offsetZ * 2048);

        map.addRenderer(pr);
    }
}
