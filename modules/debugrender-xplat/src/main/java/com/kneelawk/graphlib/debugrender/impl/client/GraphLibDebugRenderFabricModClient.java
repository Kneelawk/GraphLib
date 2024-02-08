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

package com.kneelawk.graphlib.debugrender.impl.client;

import java.util.ArrayList;
import java.util.Set;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.DebugRenderer;

@SuppressWarnings("unused")
public class GraphLibDebugRenderFabricModClient implements ClientModInitializer {
    private final LongSet loadedChunks = new LongLinkedOpenHashSet();

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        GraphLibDebugRenderClientImpl.register();

        GLClientDebugNet.init();

        DebugRenderer.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.clear();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.clear();
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> loadedChunks.add(chunk.getPos().toLong()));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            long chunkLong = chunk.getPos().toLong();
            loadedChunks.remove(chunkLong);

            Set<DebugBlockGraph> graphs = GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.get(chunkLong);
            if (graphs != null) {
                for (DebugBlockGraph graph : new ArrayList<>(graphs)) {
                    boolean anyChunkLoaded = false;
                    for (long graphChunk : graph.chunks()) {
                        if (loadedChunks.contains(graphChunk)) {
                            anyChunkLoaded = true;
                            break;
                        }
                    }

                    if (!anyChunkLoaded) {
                        Long2ObjectMap<DebugBlockGraph> universe =
                            DebugRenderer.DEBUG_GRAPHS.get(graph.universeId());
                        universe.remove(graph.graphId());
                        if (universe.isEmpty()) {
                            DebugRenderer.DEBUG_GRAPHS.remove(graph.universeId());
                        }

                        GraphLibDebugRenderClientImpl.removeGraphChunks(graph);
                    }
                }
            }
        });
    }
}
