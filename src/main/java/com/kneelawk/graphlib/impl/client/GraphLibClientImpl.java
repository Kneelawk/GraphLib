package com.kneelawk.graphlib.impl.client;

import com.kneelawk.graphlib.api.v1.client.GraphLibClient;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.api.v1.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.impl.client.render.SimpleBlockNodeRenderer;
import com.kneelawk.graphlib.impl.client.render.SimpleSidedBlockNodeRenderer;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketDecoder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.Set;

public class GraphLibClientImpl {
    private GraphLibClientImpl() {
    }

    private static final Identifier BLOCK_NODE_PACKET_DECODER_ID = Constants.id("block_node_packet_decoder");
    public static final RegistryKey<Registry<BlockNodePacketDecoder>> BLOCK_NODE_PACKET_DECODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_PACKET_DECODER_ID);

    private static final Identifier BLOCK_NODE_RENDERER_ID = Constants.id("block_node_renderer");
    public static final RegistryKey<Registry<BlockNodeRendererHolder<?>>> BLOCK_NODE_RENDERER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_RENDERER_ID);

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_PACKET_DECODER_ID,
            GraphLibClient.BLOCK_NODE_PACKET_DECODER);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_RENDERER_ID,
            GraphLibClient.BLOCK_NODE_RENDERER);

        Registry.register(GraphLibClient.BLOCK_NODE_RENDERER, Constants.id("simple"),
            new BlockNodeRendererHolder<>(SimpleClientBlockNode.class, SimpleBlockNodeRenderer.INSTANCE));
        Registry.register(GraphLibClient.BLOCK_NODE_RENDERER, Constants.id("simple_sided"),
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
