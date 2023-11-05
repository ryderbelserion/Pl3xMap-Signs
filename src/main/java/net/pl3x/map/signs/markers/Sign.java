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
import java.io.IOException;
import java.util.List;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public record Sign(@NotNull Position pos, @NotNull Icon icon, @NotNull List<String> lines) {

    public boolean isSign(@NotNull World world) {
        return world.getBlockAt(pos().x(), pos().y(), pos().z()).getState() instanceof org.bukkit.block.Sign;
    }

    public static @NotNull Sign load(@NotNull DataInputStream in) throws IOException {
        return new Sign(Position.load(in), Icon.valueOf(in.readUTF()),
                List.of(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF())
        );
    }

    public void save(@NotNull DataOutputStream out) throws IOException {
        pos().save(out);
        out.writeUTF(icon().name());
        for (String line : lines()) {
            out.writeUTF(line);
        }
    }
}