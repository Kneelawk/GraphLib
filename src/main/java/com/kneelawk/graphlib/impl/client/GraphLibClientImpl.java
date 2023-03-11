package com.kneelawk.graphlib.impl.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.api.v1.client.BlockNodePacketDecoder;
import com.kneelawk.graphlib.api.v1.client.GraphLibClient;
import com.kneelawk.graphlib.impl.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.impl.client.render.SimpleBlockNodeRenderer;
import com.kneelawk.graphlib.impl.client.render.SimpleSidedBlockNodeRenderer;

@Environment(EnvType.CLIENT)
public class GraphLibClientImpl {
    private GraphLibClientImpl() {
    }

    public static final Map<Identifier, Map<Identifier, BlockNodePacketDecoder>> DECODERS = new HashMap<>();

    public static final Map<Identifier, Map<Identifier, BlockNodeRendererHolder<?>>> RENDERERS = new HashMap<>();

    public static final Map<Identifier, BlockNodeRendererHolder<?>> ALL_UNIVERSE_RENDERERS = new HashMap<>();

    /**
     * Map of graph id long to graph for all currently debugging graphs.
     */
    public static final Map<Identifier, Long2ObjectMap<ClientBlockGraph>> DEBUG_GRAPHS = new LinkedHashMap<>();

    /**
     * Map of {@link ChunkPos#toLong()} to a set of graphs in that chunk for all currently debugging graphs.
     */
    public static final Long2ObjectMap<Set<ClientBlockGraph>> GRAPHS_PER_CHUNK = new Long2ObjectLinkedOpenHashMap<>();

    public static @Nullable BlockNodePacketDecoder getDecoder(Identifier universeId, Identifier typeId) {
        Map<Identifier, BlockNodePacketDecoder> universeDecoders = DECODERS.get(universeId);
        if (universeDecoders == null) return null;
        return universeDecoders.get(typeId);
    }

    public static @Nullable BlockNodeRendererHolder<?> getRenderer(Identifier universeId, Identifier renderId) {
        Map<Identifier, BlockNodeRendererHolder<?>> universeRenderers = RENDERERS.get(universeId);
        if (universeRenderers == null) return ALL_UNIVERSE_RENDERERS.get(renderId);
        BlockNodeRendererHolder<?> holder = universeRenderers.get(renderId);
        if (holder == null) return ALL_UNIVERSE_RENDERERS.get(renderId);
        return holder;
    }

    static void register() {
        GraphLibClient.registerRendererForAllUniverses(Constants.id("simple"), SimpleClientBlockNode.class,
            SimpleBlockNodeRenderer.INSTANCE);
        GraphLibClient.registerRendererForAllUniverses(Constants.id("simple_sided"), SimpleClientSidedBlockNode.class,
            SimpleSidedBlockNodeRenderer.INSTANCE);
    }

    static void removeGraphChunks(ClientBlockGraph graph) {
        for (long graphChunk : graph.chunks()) {
            Set<ClientBlockGraph> graphsAt = GRAPHS_PER_CHUNK.get(graphChunk);
            if (graphsAt != null) {
                graphsAt.remove(graph);

                if (graphsAt.isEmpty()) {
                    GRAPHS_PER_CHUNK.remove(graphChunk);
                }
            }
        }
    }
}
