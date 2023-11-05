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
package com.ryderbelserion.map.signs.listener;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.world.World;
import com.ryderbelserion.map.signs.markers.Icon;
import com.ryderbelserion.map.signs.markers.Position;
import com.ryderbelserion.map.signs.markers.Sign;
import com.ryderbelserion.map.signs.markers.SignsLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignEdit(@NotNull SignChangeEvent event) {
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

        tryAddSign(state, pos, event.getSide());
    }

    @EventHandler
    public void onClickSign(@NotNull PlayerInteractEvent event) {
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

            case RIGHT_CLICK_BLOCK -> {
                // cancel event to stop sign editor from opening
                event.setCancelled(true);

                BlockFace facing = event.getBlockFace();

                if (state.getBlockData() instanceof Directional directional) {
                    facing = directional.getFacing();
                }

                tryAddSign(sign, sign.getSide(event.getBlockFace() == facing ? Side.FRONT : Side.BACK));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockDropItemEvent event) {
        tryRemoveSign(event.getBlockState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockBurnEvent event) {
        tryRemoveSign(event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull EntityExplodeEvent event) {
        event.blockList().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockPistonExtendEvent event) {
        event.getBlocks().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockPistonRetractEvent event) {
        event.getBlocks().forEach(block -> tryRemoveSign(block.getState()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(@NotNull BlockFromToEvent event) {
        tryRemoveSign(event.getToBlock().getState());
    }

    private void tryAddSign(BlockState state, Position pos, Side side) {
        if (state instanceof org.bukkit.block.Sign sign) {
            tryAddSign(sign, pos, sign.getSide(side));
        }
    }

    private void tryAddSign(@NotNull BlockState state, @NotNull SignSide side) {
        if (state instanceof org.bukkit.block.Sign sign) {
            Location loc = sign.getLocation();
            Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            tryAddSign(sign, pos, side);
        }
    }

    private void tryAddSign(org.bukkit.block.Sign sign, Position pos, SignSide side) {
        SignsLayer layer = getLayer(sign);
        if (layer == null) {
            // world has no signs layer; ignore
            return;
        }

        Icon icon = Icon.get(sign);
        if (icon == null) {
            // material is not a registered sign; ignore
            return;
        }

        // add sign to layer
        layer.putSign(new Sign(pos, icon, getLines(side)));

        // play fancy particles as visualizer
        particles(sign.getLocation(), layer.getConfig().SIGN_ADD_PARTICLES, layer.getConfig().SIGN_ADD_SOUND);
    }

    protected void tryRemoveSign(@NotNull BlockState state) {
        if (state instanceof org.bukkit.block.Sign sign) {
            tryRemoveSign(sign);
        }
    }

    private void tryRemoveSign(@NotNull org.bukkit.block.Sign sign) {
        SignsLayer layer = getLayer(sign);

        if (layer == null) {
            // world has no signs layer; ignore
            return;
        }

        Location loc = sign.getLocation();
        Position pos = new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        layer.removeSign(pos);

        // play fancy particles as visualizer
        particles(sign.getLocation(), layer.getConfig().SIGN_REMOVE_PARTICLES, layer.getConfig().SIGN_REMOVE_SOUND);
    }

    protected List<String> getLines(SignSide side) {
        return List.of(side.getLines());
    }

    protected @Nullable SignsLayer getLayer(@NotNull BlockState state) {
        World world = Pl3xMap.api().getWorldRegistry().get(state.getWorld().getName());

        if (world == null || !world.isEnabled()) {
            // world is missing or not enabled; ignore
            return null;
        }

        return (SignsLayer) world.getLayerRegistry().get(SignsLayer.KEY);
    }

    protected void particles(@NotNull Location loc, @Nullable Particle particle, @Nullable Sound sound) {

        if (sound != null) {
            loc.getWorld().playSound(loc, sound, 1.0F, 1.0F);
        }

        if (particle != null) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();

            for (int i = 0; i < 20; ++i) {
                double x = loc.getX() + rand.nextGaussian();
                double y = loc.getY() + rand.nextGaussian();
                double z = loc.getZ() + rand.nextGaussian();
                loc.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, null, true);
            }
        }
    }
}