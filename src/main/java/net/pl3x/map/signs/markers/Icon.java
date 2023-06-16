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
import java.util.Locale;
import javax.imageio.ImageIO;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.signs.Pl3xMapSigns;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Icon {
    ACACIA, BAMBOO, BIRCH, CHERRY, CRIMSON, DARK_OAK, JUNGLE, MANGROVE, OAK, SPRUCE, WARPED;

    private final String type;
    private final String key;

    Icon() {
        this.type = name().toLowerCase(Locale.ROOT);
        this.key = String.format("pl3xmap_%s_sign", this.type);
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

    public static @Nullable Icon get(@NotNull Material type) {
        return switch (type) {
            case ACACIA_SIGN, ACACIA_WALL_SIGN, ACACIA_HANGING_SIGN, ACACIA_WALL_HANGING_SIGN -> ACACIA;
            case BAMBOO_SIGN, BAMBOO_WALL_SIGN, BAMBOO_HANGING_SIGN, BAMBOO_WALL_HANGING_SIGN -> BAMBOO;
            case BIRCH_SIGN, BIRCH_WALL_SIGN, BIRCH_HANGING_SIGN, BIRCH_WALL_HANGING_SIGN -> BIRCH;
            case CHERRY_SIGN, CHERRY_WALL_SIGN, CHERRY_HANGING_SIGN, CHERRY_WALL_HANGING_SIGN -> CHERRY;
            case CRIMSON_SIGN, CRIMSON_WALL_SIGN, CRIMSON_HANGING_SIGN, CRIMSON_WALL_HANGING_SIGN -> CRIMSON;
            case DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, DARK_OAK_HANGING_SIGN, DARK_OAK_WALL_HANGING_SIGN -> DARK_OAK;
            case JUNGLE_SIGN, JUNGLE_WALL_SIGN, JUNGLE_HANGING_SIGN, JUNGLE_WALL_HANGING_SIGN -> JUNGLE;
            case MANGROVE_SIGN, MANGROVE_WALL_SIGN, MANGROVE_HANGING_SIGN, MANGROVE_WALL_HANGING_SIGN -> MANGROVE;
            case OAK_SIGN, OAK_WALL_SIGN, OAK_HANGING_SIGN, OAK_WALL_HANGING_SIGN -> OAK;
            case SPRUCE_SIGN, SPRUCE_WALL_SIGN, SPRUCE_HANGING_SIGN, SPRUCE_WALL_HANGING_SIGN -> SPRUCE;
            case WARPED_SIGN, WARPED_WALL_SIGN, WARPED_HANGING_SIGN, WARPED_WALL_HANGING_SIGN -> WARPED;
            default -> null;
        };
    }

    public static void register() {
        Pl3xMapSigns plugin = Pl3xMapSigns.getPlugin(Pl3xMapSigns.class);
        for (Icon icon : values()) {
            String signFilename = String.format("icons%s%s_sign.png", File.separator, icon.type);
            File signFile = new File(plugin.getDataFolder(), signFilename);
            if (!signFile.exists()) {
                plugin.saveResource(signFilename, false);
            }

            String tooltipKey = String.format("pl3xmap_%s_sign_tooltip", icon.type);
            String tooltipFilename = String.format("icons%s%s_tooltip.png", File.separator, icon.type);
            File tooltipFile = new File(plugin.getDataFolder(), tooltipFilename);
            if (!tooltipFile.exists()) {
                plugin.saveResource(tooltipFilename, false);
            }

            try {
                Pl3xMap.api().getIconRegistry().register(new IconImage(icon.key, ImageIO.read(signFile), "png"));
                Pl3xMap.api().getIconRegistry().register(new IconImage(tooltipKey, ImageIO.read(tooltipFile), "png"));
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to register icon (" + icon.type + ") " + signFilename);
                e.printStackTrace();
            }
        }
    }
}
