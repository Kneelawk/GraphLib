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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.debugrender.api.client.BlockNodeDebugPacketDecoder;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.client.GraphLibDebugRenderClient;
import com.kneelawk.graphlib.debugrender.impl.client.debug.graph.SimpleDebugBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.debug.graph.SimpleDebugSidedBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.BlockNodeDebugRendererHolder;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.DebugRenderer;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.SimpleBlockNodeDebugRenderer;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.SimpleSidedBlockNodeDebugRenderer;
import com.kneelawk.graphlib.impl.Constants;

@Environment(EnvType.CLIENT)
public class GraphLibDebugRenderClientImpl {
    private GraphLibDebugRenderClientImpl() {
    }

    private static final LongSet loadedChunks = new LongLinkedOpenHashSet();

    public static final Map<Identifier, Map<Identifier, BlockNodeDebugPacketDecoder>> DEBUG_DECODERS = new HashMap<>();

    public static final Map<Identifier, Map<Identifier, BlockNodeDebugRendererHolder<?>>> DEBUG_RENDERERS =
        new HashMap<>();

    public static final Map<Identifier, BlockNodeDebugRendererHolder<?>> ALL_UNIVERSE_DEBUG_RENDERERS = new HashMap<>();

    /**
     * Map of {@link ChunkPos#toLong()} to a set of graphs in that chunk for all currently debugging graphs.
     */
    public static final Long2ObjectMap<Set<DebugBlockGraph>> GRAPHS_PER_CHUNK = new Long2ObjectLinkedOpenHashMap<>();

    public static @Nullable BlockNodeDebugPacketDecoder getDebugDecoder(Identifier universeId, Identifier typeId) {
        Map<Identifier, BlockNodeDebugPacketDecoder> universeDecoders = DEBUG_DECODERS.get(universeId);
        if (universeDecoders == null) return null;
        return universeDecoders.get(typeId);
    }

    public static @Nullable BlockNodeDebugRendererHolder<?> getDebugRenderer(Identifier universeId,
                                                                             Identifier renderId) {
        Map<Identifier, BlockNodeDebugRendererHolder<?>> universeRenderers = DEBUG_RENDERERS.get(universeId);
        if (universeRenderers == null) return ALL_UNIVERSE_DEBUG_RENDERERS.get(renderId);
        BlockNodeDebugRendererHolder<?> holder = universeRenderers.get(renderId);
        if (holder == null) return ALL_UNIVERSE_DEBUG_RENDERERS.get(renderId);
        return holder;
    }

    public static void register() {
        GraphLibDebugRenderClient.registerDebugRendererForAllUniverses(Constants.id("simple"), SimpleDebugBlockNode.class,
            SimpleBlockNodeDebugRenderer.INSTANCE);
        GraphLibDebugRenderClient.registerDebugRendererForAllUniverses(Constants.id("simple_sided"),
            SimpleDebugSidedBlockNode.class,
            SimpleSidedBlockNodeDebugRenderer.INSTANCE);
    }

    public static void chunkLoad(long chunkLong) {
        loadedChunks.add(chunkLong);
    }

    public static void chunkUnload(long chunkLong) {
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
    }

    public static void removeGraphChunks(DebugBlockGraph graph) {
        for (long graphChunk : graph.chunks()) {
            Set<DebugBlockGraph> graphsAt = GRAPHS_PER_CHUNK.get(graphChunk);
            if (graphsAt != null) {
                graphsAt.remove(graph);

                if (graphsAt.isEmpty()) {
                    GRAPHS_PER_CHUNK.remove(graphChunk);
                }
            }
        }
    }
}
