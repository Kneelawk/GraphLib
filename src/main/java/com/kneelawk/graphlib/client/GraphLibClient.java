package com.kneelawk.graphlib.client;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.client.render.SimpleBlockNodeRenderer;
import com.kneelawk.graphlib.client.render.SimpleSidedBlockNodeRenderer;
import com.kneelawk.graphlib.net.BlockNodePacketDecoder;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

import java.util.Set;

/**
 * Graph Lib Client-Side Public API. This class contains static methods and fields for registering
 * {@link BlockNodePacketDecoder}s and renderers.
 */
public final class GraphLibClient {
    private GraphLibClient() {
    }

    private static final Identifier BLOCK_NODE_PACKET_DECODER_ID = Constants.id("block_node_packet_decoder");
    private static final RegistryKey<Registry<BlockNodePacketDecoder>> BLOCK_NODE_PACKET_DECODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_PACKET_DECODER_ID);

    private static final Identifier BLOCK_NODE_RENDERER_ID = Constants.id("block_node_renderer");
    private static final RegistryKey<Registry<BlockNodeRendererHolder<?>>> BLOCK_NODE_RENDERER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_RENDERER_ID);

    /**
     * Registry for custom block node decoders.
     */
    public static final Registry<BlockNodePacketDecoder> BLOCK_NODE_PACKET_DECODER =
        new SimpleRegistry<>(BLOCK_NODE_PACKET_DECODER_KEY, Lifecycle.experimental());

    /**
     * Registry for custom block node renderers.
     */
    public static final Registry<BlockNodeRendererHolder<?>> BLOCK_NODE_RENDERER =
        new SimpleRegistry<>(BLOCK_NODE_RENDERER_KEY, Lifecycle.experimental());

    /**
     * Map of graph id long to graph for all currently debugging graphs.
     */
    public static final Long2ObjectMap<ClientBlockGraph> DEBUG_GRAPHS = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Map of {@link ChunkPos#toLong()} to a set of graphs in that chunk for all currently debugging graphs.
     */
    public static final Long2ObjectMap<Set<ClientBlockGraph>> GRAPHS_PER_CHUNK = new Long2ObjectLinkedOpenHashMap<>();

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_PACKET_DECODER_ID,
            BLOCK_NODE_PACKET_DECODER);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_RENDERER_ID,
            BLOCK_NODE_RENDERER);

        Registry.register(BLOCK_NODE_RENDERER, Constants.id("simple"),
            new BlockNodeRendererHolder<>(SimpleClientBlockNode.class, SimpleBlockNodeRenderer.INSTANCE));
        Registry.register(BLOCK_NODE_RENDERER, Constants.id("simple_sided"),
            new BlockNodeRendererHolder<>(SimpleClientSidedBlockNode.class, SimpleSidedBlockNodeRenderer.INSTANCE));
    }

    static void removeGraphChunks(ClientBlockGraph graph) {
        for (long graphChunk : graph.chunks()) {
            Set<ClientBlockGraph> graphsAt = GraphLibClient.GRAPHS_PER_CHUNK.get(graphChunk);
            if (graphsAt != null) {
                graphsAt.remove(graph);

                if (graphsAt.isEmpty()) {
                    GraphLibClient.GRAPHS_PER_CHUNK.remove(graphChunk);
                }
            }
        }
    }
}
