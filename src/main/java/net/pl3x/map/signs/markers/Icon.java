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
package net.pl3x.map.signs.markers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.signs.Pl3xMapSigns;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Icon {
    ACACIA, BAMBOO, BIRCH, CHERRY, CRIMSON, DARK_OAK, JUNGLE, MANGROVE, OAK, SPRUCE, WARPED;

    private final String name;
    private final String key;

    Icon() {
        this.name = name().toLowerCase(Locale.ROOT);
        this.key = String.format("pl3xmap_%s_sign", this.name);
    }

    public @NotNull String getKey() {
        return this.key;
    }

    public static void saveGimpSrc() {
        Pl3xMapSigns plugin = Pl3xMapSigns.getPlugin(Pl3xMapSigns.class);
        String filename = String.format("icons%ssigns.xcf", File.separator);
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }
    }

    private static final Map<String, Icon> BY_NAME = new HashMap<>();
    private static final Map<WoodType, Icon> BY_WOOD = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(icon -> BY_NAME.put(icon.name, icon));
        WoodType.values().forEach(type -> BY_WOOD.computeIfAbsent(type, k -> BY_NAME.get(type.name())));
    }

    public static @Nullable Icon get(@NotNull Sign sign) {
        return BY_WOOD.get(((SignBlock) ((CraftBlock) sign.getBlock()).getNMS().getBlock()).type());
    }

    public static void register() {
        Pl3xMapSigns plugin = Pl3xMapSigns.getPlugin(Pl3xMapSigns.class);
        for (Icon icon : values()) {
            String signFilename = String.format("icons%s%s_sign.png", File.separator, icon.name);
            File signFile = new File(plugin.getDataFolder(), signFilename);
            if (!signFile.exists()) {
                plugin.saveResource(signFilename, false);
            }

            String tooltipKey = String.format("pl3xmap_%s_sign_tooltip", icon.name);
            String tooltipFilename = String.format("icons%s%s_tooltip.png", File.separator, icon.name);
            File tooltipFile = new File(plugin.getDataFolder(), tooltipFilename);
            if (!tooltipFile.exists()) {
                plugin.saveResource(tooltipFilename, false);
            }

            try {
                Pl3xMap.api().getIconRegistry().register(new IconImage(icon.key, ImageIO.read(signFile), "png"));
                Pl3xMap.api().getIconRegistry().register(new IconImage(tooltipKey, ImageIO.read(tooltipFile), "png"));
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to register icon (" + icon.name + ") " + signFilename);
                e.printStackTrace();
            }
        }
    }
}
