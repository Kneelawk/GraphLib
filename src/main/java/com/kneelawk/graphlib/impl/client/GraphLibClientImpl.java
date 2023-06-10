package com.kneelawk.graphlib.impl.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.api.client.BlockNodeDebugPacketDecoder;
import com.kneelawk.graphlib.api.client.GraphLibClient;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.client.debug.graph.DebugBlockGraph;
import com.kneelawk.graphlib.impl.client.debug.graph.SimpleDebugBlockNode;
import com.kneelawk.graphlib.impl.client.debug.graph.SimpleDebugSidedBlockNode;
import com.kneelawk.graphlib.impl.client.debug.render.BlockNodeDebugRendererHolder;
import com.kneelawk.graphlib.impl.client.debug.render.SimpleBlockNodeDebugRenderer;
import com.kneelawk.graphlib.impl.client.debug.render.SimpleSidedBlockNodeDebugRenderer;

@Environment(EnvType.CLIENT)
public class GraphLibClientImpl {
    private GraphLibClientImpl() {
    }

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

    static void register() {
        GraphLibClient.registerDebugRendererForAllUniverses(Constants.id("simple"), SimpleDebugBlockNode.class,
            SimpleBlockNodeDebugRenderer.INSTANCE);
        GraphLibClient.registerDebugRendererForAllUniverses(Constants.id("simple_sided"),
            SimpleDebugSidedBlockNode.class,
            SimpleSidedBlockNodeDebugRenderer.INSTANCE);
    }

    static void removeGraphChunks(DebugBlockGraph graph) {
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
