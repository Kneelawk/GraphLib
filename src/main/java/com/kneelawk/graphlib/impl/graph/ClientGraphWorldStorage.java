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

package com.kneelawk.graphlib.impl.graph;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class ClientGraphWorldStorage implements GraphWorldStorage {
    private final Map<Identifier, ClientGraphWorldImpl> worlds = new Object2ObjectLinkedOpenHashMap<>();
    private final World clientWorld;

    public ClientGraphWorldStorage(World clientWorld, int loadDistance) {
        this.clientWorld = clientWorld;

        for (GraphUniverseImpl universe : GraphLibImpl.UNIVERSE) {
            if (universe.getSyncProfile().isEnabled()) {
                Identifier universeId = universe.getId();

                worlds.put(universeId, universe.createClientGraphWorld(clientWorld, loadDistance));
            }
        }
    }

    @Override
    public @NotNull ClientGraphWorldImpl get(@NotNull Identifier universeId) {
        if (!worlds.containsKey(universeId)) {
            throw new IllegalStateException(
                "Attempted to get a client graph world for a universe that has not been synchronized. Make sure your universe builder's synchronizeToClient(...) is called with something that allows synchronization. Universe: " +
                    universeId);
        }

        return worlds.get(universeId);
    }

    public void unload(ChunkPos pos) {
        for (ClientGraphWorldImpl impl : worlds.values()) {
            try {
                impl.unload(pos);
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in client GraphWorld. World: '{}'/{}, Chunk: {}", clientWorld,
                    clientWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void setChunkMapCenter(int x, int z) {
        for (ClientGraphWorldImpl impl : worlds.values()) {
            try {
                impl.setChunkMapCenter(x, z);
            } catch (Exception e) {
                GLLog.error("Error setting center chunk in client GraphWorld. World: '{}'/{}, Chunk: ({}, {})",
                    clientWorld, clientWorld.getRegistryKey().getValue(), x, z, e);
            }
        }
    }

    public void updateLoadDistance(int loadDistance) {
        for (ClientGraphWorldImpl impl : worlds.values()) {
            try {
                impl.updateLoadDistance(loadDistance);
            } catch (Exception | OutOfMemoryError e) {
                GLLog.error("Error setting load distance in client GraphWorld. World: '{}'/{}, Load distance: {}",
                    clientWorld, clientWorld.getRegistryKey().getValue(), loadDistance, e);
            }
        }
    }
}
