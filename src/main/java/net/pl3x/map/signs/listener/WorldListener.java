/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.pl3x.map.signs.listener;

import libs.org.checkerframework.checker.nullness.qual.NonNull;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.world.World;
import net.pl3x.map.signs.configuration.WorldConfig;
import net.pl3x.map.signs.markers.Icon;
import net.pl3x.map.signs.markers.SignsLayer;
import org.bukkit.Chunk;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListener implements EventListener, Listener {
    public WorldListener() {
        Pl3xMap.api().getEventRegistry().register(this);
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            // chunk is new; ignore
            return;
        }
        checkChunk(event.getChunk());
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        checkChunk(event.getChunk());
    }

    @EventHandler
    public void onServerLoaded(@NonNull ServerLoadedEvent event) {
        Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
        Icon.saveGimpSrc();
    }

    @EventHandler
    public void onWorldLoaded(@NonNull WorldLoadedEvent event) {
        registerWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(@NonNull WorldUnloadedEvent event) {
        try {
            event.getWorld().getLayerRegistry().unregister(SignsLayer.KEY);
        } catch (Throwable ignore) {
        }
    }

    private void registerWorld(@NonNull World world) {
        world.getLayerRegistry().register(new SignsLayer(new WorldConfig(world)));
    }

    private void checkChunk(Chunk chunk) {
        org.bukkit.World bukkitWorld = chunk.getWorld();

        World world = Pl3xMap.api().getWorldRegistry().get(bukkitWorld.getName());
        if (world == null) {
            // world is missing or not enabled; ignore
            return;
        }

        SignsLayer layer = (SignsLayer) world.getLayerRegistry().get(SignsLayer.KEY);
        if (layer == null) {
            // world has no signs layer; ignore
            return;
        }


        int minX = chunk.getX();
        int minZ = chunk.getZ();
        int maxX = minX + 16;
        int maxZ = minZ + 16;

        layer.getSigns().stream()
                // filter signs only inside chunk
                .filter(sign -> sign.pos().x() >= minX)
                .filter(sign -> sign.pos().z() >= minZ)
                .filter(sign -> sign.pos().x() <= maxX)
                .filter(sign -> sign.pos().z() <= maxZ)
                // filter signs that are no longer there
                .filter(sign -> !sign.isSign(bukkitWorld))
                // remove all matching signs
                .forEach(sign -> layer.removeSign(sign.pos()));
    }
}
