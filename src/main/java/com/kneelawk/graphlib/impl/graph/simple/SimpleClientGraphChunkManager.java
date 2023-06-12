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

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.World;

public class SimpleClientGraphChunkManager {
    volatile ClientPillarMap pillars;
    final World world;
    private final UnloadListener unloadListener;

    public SimpleClientGraphChunkManager(int loadDistance, World world, UnloadListener unloadListener) {
        pillars = new ClientPillarMap(getChunkMapRadius(loadDistance));
        this.world = world;
        this.unloadListener = unloadListener;
    }

    private static int getChunkMapRadius(int loadDistance) {
        return Math.max(2, loadDistance) + 3;
    }

    private static boolean positionEquals(@Nullable SimpleBlockGraphPillar pillar, int x, int z) {
        if (pillar == null) {
            return false;
        } else {
            return pillar.x == x && pillar.z == z;
        }
    }

    public void unload(int chunkX, int chunkZ) {
        if (pillars.isInRadius(chunkX, chunkZ)) {
            int i = pillars.getIndex(chunkX, chunkZ);
            SimpleBlockGraphPillar pillar = pillars.getPillar(i);
            if (positionEquals(pillar, chunkX, chunkZ)) {
                unloadListener.onPillarUnload(pillar);
                pillars.compareAndSet(i, pillar, null);
            }
        }
    }

    public @Nullable SimpleBlockGraphPillar getPillar(int chunkX, int chunkZ) {
        if (pillars.isInRadius(chunkX, chunkZ)) {
            SimpleBlockGraphPillar pillar = pillars.getPillar(pillars.getIndex(chunkX, chunkZ));
            if (positionEquals(pillar, chunkX, chunkZ)) {
                return pillar;
            }
        }

        return null;
    }

    public @Nullable SimpleBlockGraphPillar initPillar(int x, int z) {
        if (!pillars.isInRadius(x, z)) {
            return null;
        } else {
            int i = pillars.getIndex(x, z);
            SimpleBlockGraphPillar pillar = pillars.pillars.get(i);
            if (!positionEquals(pillar, x, z)) {
                if (pillar != null) {
                    unloadListener.onPillarUnload(pillar);
                }
                pillar = new SimpleBlockGraphPillar(x, z, world);
                pillars.set(i, pillar);
            }

            return pillar;
        }
    }

    public void setPillarMapCenter(int x, int z) {
        pillars.centerChunkX = x;
        pillars.centerChunkZ = z;
    }

    public void updateLoadDistance(int loadDistance) {
        int oldRadius = pillars.radius;
        int newRadius = getChunkMapRadius(loadDistance);

        if (oldRadius != newRadius) {
            ClientPillarMap map = new ClientPillarMap(newRadius);
            map.centerChunkX = pillars.centerChunkX;
            map.centerChunkZ = pillars.centerChunkZ;

            for (int i = 0; i < pillars.pillars.length(); i++) {
                SimpleBlockGraphPillar pillar = pillars.pillars.get(i);
                if (pillar != null) {
                    if (map.isInRadius(pillar.x, pillar.z)) {
                        map.set(map.getIndex(pillar.x, pillar.z), pillar);
                    } else {
                        unloadListener.onPillarUnload(pillar);
                    }
                }
            }

            pillars = map;
        }
    }

    public int getLoadedPillarCount() {
        return pillars.loadedPillarCount;
    }

    final class ClientPillarMap {
        final AtomicReferenceArray<@Nullable SimpleBlockGraphPillar> pillars;
        final int radius;
        private final int diameter;
        volatile int centerChunkX;
        volatile int centerChunkZ;
        int loadedPillarCount;

        ClientPillarMap(int radius) {
            this.radius = radius;
            this.diameter = radius * 2 + 1;
            this.pillars = new AtomicReferenceArray<>(diameter * diameter);
        }

        int getIndex(int chunkX, int chunkZ) {
            return Math.floorMod(chunkZ, diameter) * diameter + Math.floorMod(chunkX, diameter);
        }

        protected void set(int index, @Nullable SimpleBlockGraphPillar pillar) {
            @Nullable SimpleBlockGraphPillar oldPillar = pillars.getAndSet(index, pillar);
            if (oldPillar != null) {
                loadedPillarCount--;
            }

            if (pillar != null) {
                loadedPillarCount++;
            }
        }

        protected SimpleBlockGraphPillar compareAndSet(int index, SimpleBlockGraphPillar expect,
                                                       SimpleBlockGraphPillar update) {
            if (pillars.compareAndSet(index, expect, update) && update == null) {
                loadedPillarCount--;
            }

            return expect;
        }

        boolean isInRadius(int chunkX, int chunkZ) {
            return Math.abs(chunkX - centerChunkX) <= radius && Math.abs(chunkZ - centerChunkZ) <= radius;
        }

        protected @Nullable SimpleBlockGraphPillar getPillar(int index) {
            return pillars.get(index);
        }
    }

    public interface UnloadListener {
        void onPillarUnload(@NotNull SimpleBlockGraphPillar pillar);
    }
}
