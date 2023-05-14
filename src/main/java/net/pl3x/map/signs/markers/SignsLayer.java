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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.signs.configuration.WorldConfig;
import org.jetbrains.annotations.NotNull;

public class SignsLayer extends WorldLayer {
    public static final String KEY = "pl3xmap_signs";

    private final Path dataFile;
    private final WorldConfig config;
    private final Options options;

    private final Map<Position, Sign> signs = new ConcurrentHashMap<>();

    public SignsLayer(@NotNull WorldConfig config) {
        super(KEY, config.getWorld(), () -> config.LAYER_LABEL);
        this.config = config;
        this.dataFile = getWorld().getTilesDirectory().resolve("signs.dat");

        setShowControls(config.LAYER_SHOW_CONTROLS);
        setDefaultHidden(config.LAYER_DEFAULT_HIDDEN);
        setUpdateInterval(config.LAYER_UPDATE_INTERVAL);
        setPriority(config.LAYER_PRIORITY);
        setZIndex(config.LAYER_ZINDEX);
        setCss(config.LAYER_CSS);
        this.options = new Options.Builder()
                .popupOffset(Point.of(0, 10))
                .popupMaxWidth(196)
                .popupMinWidth(196)
                .popupMaxHeight(210)
                .popupCloseButton(false)
                .build();

        loadData();
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return this.signs.values().stream().map(sign -> {
            String key = String.format("%s_%s_%d_%d", KEY, getWorld().getName(), sign.pos().x(), sign.pos().z());
            return Marker.icon(key, sign.pos().toPoint(), sign.icon().getKey(), this.config.ICON_SIZE)
                    .setOptions(this.options.asBuilder()
                            .popupPane(String.format("%s_popup", sign.icon().getKey()))
                            .popupContent(config.ICON_POPUP_CONTENT
                                    .replace("<line1>", sign.lines().get(0))
                                    .replace("<line2>", sign.lines().get(1))
                                    .replace("<line3>", sign.lines().get(2))
                                    .replace("<line4>", sign.lines().get(3))
                            ).build());
        }).collect(Collectors.toList());
    }

    public @NotNull Collection<Sign> getSigns() {
        return Collections.unmodifiableCollection(this.signs.values());
    }

    public boolean hasSign(@NotNull Position pos) {
        return this.signs.containsKey(pos);
    }

    public void putSign(@NotNull Sign sign) {
        this.signs.put(sign.pos(), sign);
        saveData();
    }

    public void removeSign(@NotNull Position pos) {
        this.signs.remove(pos);
        saveData();
    }

    private void loadData() {
        if (!Files.exists(this.dataFile)) {
            return;
        }
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(this.dataFile.toFile())))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                Sign sign = Sign.load(in);
                this.signs.put(sign.pos(), sign);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void saveData() {
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(this.dataFile.toFile())))) {
            out.writeInt(this.signs.size());
            for (Sign sign : this.signs.values()) {
                sign.save(out);
            }
            out.flush();
        } catch (Throwable ignore) {
        }
    }
}
