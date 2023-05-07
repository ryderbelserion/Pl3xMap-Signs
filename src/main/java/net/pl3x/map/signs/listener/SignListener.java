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

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.world.World;
import net.pl3x.map.signs.markers.Icon;
import net.pl3x.map.signs.markers.Position;
import net.pl3x.map.signs.markers.Sign;
import net.pl3x.map.signs.markers.SignsLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignEdit(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("pl3xmap.signs.admin")) {
            // player doesn't have permission to track signs; ignore
            return;
        }

        BlockState state = event.getBlock().getState();

        SignsLayer layer = getLayer(state);
        if (layer == null) {
            // world doesn't have a signs layer; ignore
            return;
        }

        Location loc = state.getLocation();
        Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        if (!layer.hasSign(pos)) {
            // not tracking any signs here; ignore
            return;
        }

        tryAddSign(state, pos, event.lines());
    }

    @EventHandler
    public void onClickSign(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            // no block was clicked; ignore
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Sign sign)) {
            // clicked block is not a sign; ignore
            return;
        }

        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.FILLED_MAP) {
            // player was not holding a filled map; ignore
            return;
        }

        if (!event.getPlayer().hasPermission("pl3xmap.signs.admin")) {
            // player does not have permission; ignore
            return;
        }

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                // cancel event to stop sign from breaking
                event.setCancelled(true);
                tryRemoveSign(sign);
            }
            case RIGHT_CLICK_BLOCK -> tryAddSign(sign);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockDropItemEvent event) {
        tryRemoveSign(event.getBlockState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockDestroyEvent event) {
        tryRemoveSign(event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBurnEvent event) {
        tryRemoveSign(event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(EntityExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockPistonExtendEvent event) {
        event.getBlocks().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockPistonRetractEvent event) {
        event.getBlocks().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockFromToEvent event) {
        tryRemoveSign(event.getToBlock().getState());
    }

    private void tryAddSign(BlockState state) {
        if (state instanceof org.bukkit.block.Sign sign) {
            Location loc = sign.getLocation();
            Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            tryAddSign(sign, pos, sign.lines());
        }
    }

    private void tryAddSign(BlockState state, Position pos, List<Component> components) {
        if (state instanceof org.bukkit.block.Sign sign) {
            tryAddSign(sign, pos, components);
        }
    }

    private void tryAddSign(org.bukkit.block.Sign sign, Position pos, List<Component> components) {
        SignsLayer layer = getLayer(sign);
        if (layer == null) {
            // world has no signs layer; ignore
            return;
        }

        Icon icon = Icon.get(sign.getType());
        if (icon == null) {
            // material is not a registered sign; ignore
            return;
        }

        List<String> lines = components.stream()
                // todo create component->html serializer
                .map(line -> PlainTextComponentSerializer.plainText().serialize(line))
                .toList();

        layer.putSign(new Sign(pos, icon, lines));

        // play fancy particles as visualizer
        particles(sign.getLocation(), Particle.VILLAGER_HAPPY, Sound.ENTITY_PLAYER_LEVELUP);
    }

    private void tryRemoveSign(BlockState state) {
        if (state instanceof org.bukkit.block.Sign sign) {
            tryRemoveSign(sign);
        }
    }

    private void tryRemoveSign(org.bukkit.block.Sign sign) {
        SignsLayer layer = getLayer(sign);
        if (layer == null) {
            // world has no signs layer; ignore
            return;
        }

        Location loc = sign.getLocation();
        Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        layer.removeSign(pos);

        // play fancy particles as visualizer
        particles(sign.getLocation(), Particle.WAX_ON, Sound.ENTITY_GHAST_HURT);
    }

    private SignsLayer getLayer(BlockState state) {
        World world = Pl3xMap.api().getWorldRegistry().get(state.getWorld().getName());
        if (world == null || !world.isEnabled()) {
            // world is missing or not enabled; ignore
            return null;
        }
        return (SignsLayer) world.getLayerRegistry().get(SignsLayer.KEY);
    }

    private void particles(Location loc, Particle particle, Sound sound) {
        loc.getWorld().playSound(loc, sound, 1.0F, 1.0F);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < 20; ++i) {
            double x = loc.getX() + rand.nextGaussian();
            double y = loc.getY() + rand.nextGaussian();
            double z = loc.getZ() + rand.nextGaussian();
            loc.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, null, true);
        }
    }
}
