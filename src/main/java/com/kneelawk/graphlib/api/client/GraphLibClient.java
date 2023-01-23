package com.kneelawk.graphlib.api.client;

import com.kneelawk.graphlib.impl.client.GraphLibClientImpl;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.api.net.BlockNodePacketDecoder;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.math.ChunkPos;

import java.util.Set;

/**
 * Graph Lib Client-Side Public API. This class contains static methods and fields for registering
 * {@link BlockNodePacketDecoder}s and renderers.
 */
public final class GraphLibClient {
    private GraphLibClient() {
    }

    /**
     * Registry for custom block node decoders.
     */
    public static final Registry<BlockNodePacketDecoder> BLOCK_NODE_PACKET_DECODER =
        new SimpleRegistry<>(GraphLibClientImpl.BLOCK_NODE_PACKET_DECODER_KEY, Lifecycle.experimental());

    /**
     * Registry for custom block node renderers.
     */
    public static final Registry<BlockNodeRendererHolder<?>> BLOCK_NODE_RENDERER =
        new SimpleRegistry<>(GraphLibClientImpl.BLOCK_NODE_RENDERER_KEY, Lifecycle.experimental());

    /**
     * Map of graph id long to graph for all currently debugging graphs.
     */
    public static final Long2ObjectMap<ClientBlockGraph> DEBUG_GRAPHS = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Map of {@link ChunkPos#toLong()} to a set of graphs in that chunk for all currently debugging graphs.
     */
    public static final Long2ObjectMap<Set<ClientBlockGraph>> GRAPHS_PER_CHUNK = new Long2ObjectLinkedOpenHashMap<>();
}
