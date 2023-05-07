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

public enum Icon {
    ACACIA, BAMBOO, BIRCH, CHERRY, CRIMSON, DARK_OAK, JUNGLE, MANGROVE, OAK, SPRUCE, WARPED;

    private final String key;

    Icon() {
        Pl3xMapSigns plugin = Pl3xMapSigns.getPlugin(Pl3xMapSigns.class);

        String signType = name().toLowerCase(Locale.ROOT);

        this.key = String.format("pl3xmap_%s_sign", signType);
        String signFilename = String.format("icons%s%s_sign.png", File.separator, signType);
        File signFile = new File(plugin.getDataFolder(), signFilename);
        if (!signFile.exists()) {
            plugin.saveResource(signFilename, false);
        }

        String tooltipKey = String.format("pl3xmap_%s_sign_tooltip", signType);
        String tooltipFilename = String.format("icons%s%s_tooltip.png", File.separator, signType);
        File tooltipFile = new File(plugin.getDataFolder(), tooltipFilename);
        if (!tooltipFile.exists()) {
            plugin.saveResource(tooltipFilename, false);
        }

        try {
            Pl3xMap.api().getIconRegistry().register(new IconImage(this.key, ImageIO.read(signFile), "png"));
            Pl3xMap.api().getIconRegistry().register(new IconImage(tooltipKey, ImageIO.read(tooltipFile), "png"));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to register icon (" + signType + ") " + signFilename);
            e.printStackTrace();
        }
    }

    public String getKey() {
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

    @SuppressWarnings("UnstableApiUsage")
    public static Icon get(Material type) {
        return switch (type) {
            case ACACIA_SIGN, ACACIA_WALL_SIGN -> ACACIA;
            case BAMBOO_SIGN, BAMBOO_WALL_SIGN -> BAMBOO;
            case BIRCH_SIGN, BIRCH_WALL_SIGN -> BIRCH;
            case CHERRY_SIGN, CHERRY_WALL_SIGN -> CHERRY;
            case CRIMSON_SIGN, CRIMSON_WALL_SIGN -> CRIMSON;
            case DARK_OAK_SIGN, DARK_OAK_WALL_SIGN -> DARK_OAK;
            case JUNGLE_SIGN, JUNGLE_WALL_SIGN -> JUNGLE;
            case MANGROVE_SIGN, MANGROVE_WALL_SIGN -> MANGROVE;
            case SPRUCE_SIGN, SPRUCE_WALL_SIGN -> SPRUCE;
            case WARPED_SIGN, WARPED_WALL_SIGN -> WARPED;
            default -> OAK;
        };
    }
}
