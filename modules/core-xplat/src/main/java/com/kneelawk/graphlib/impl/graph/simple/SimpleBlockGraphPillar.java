/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
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
 *
 */

package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public class SimpleBlockGraphPillar {
    public final int x;
    public final int z;
    public final @Nullable SimpleBlockGraphChunk @NotNull [] pillar;
    public final int bottomSectionCoord;

    public SimpleBlockGraphPillar(int x, int z, World world) {
        this(x, z, new SimpleBlockGraphChunk[world.getTopSectionCoord() - world.getBottomSectionCoord()],
            world.getBottomSectionCoord());
    }

    public SimpleBlockGraphPillar(int x, int z, @Nullable SimpleBlockGraphChunk @NotNull [] pillar,
                                  int bottomSectionCoord) {
        this.x = x;
        this.z = z;
        this.pillar = pillar;
        this.bottomSectionCoord = bottomSectionCoord;
    }

    public @Nullable SimpleBlockGraphChunk get(int y) {
        return pillar[y - bottomSectionCoord];
    }

    public @NotNull SimpleBlockGraphChunk getOrCreate(int y) {
        int i = y - bottomSectionCoord;
        SimpleBlockGraphChunk chunk = pillar[i];
        if (chunk == null) {
            chunk = new SimpleBlockGraphChunk(ChunkSectionPos.from(x, y, z), () -> {});
            pillar[i] = chunk;
        }
        return chunk;
    }

    public int getHeight() {
        return pillar.length;
    }
}
