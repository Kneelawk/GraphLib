package com.kneelawk.graphlib.api.v1.client;

import java.util.HashMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.v1.client.render.BlockNodeRenderer;
import com.kneelawk.graphlib.impl.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.api.v1.graph.GraphView;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.v1.util.graph.Node;
import com.kneelawk.graphlib.impl.client.GraphLibClientImpl;

/**
 * Graph Lib Client-Side Public API. This class contains static methods and fields for registering
 * {@link BlockNodePacketDecoder}s and renderers.
 */
@Environment(EnvType.CLIENT)
public final class GraphLibClient {
    private GraphLibClient() {
    }

    /**
     * Registers a {@link BlockNodePacketDecoder} in the given universe for the given block node type id.
     * <p>
     * Only register a decoder if you implement
     * {@link BlockNode#toPacket(ServerWorld, GraphView, BlockPos, Node, PacketByteBuf)}
     * to provide custom node data to the client.
     *
     * @param universeId the universe this decoder is to be registered under.
     * @param typeId     the block node type id this decoder is to be registered for.
     * @param decoder    the decoder.
     */
    public static void registerDecoder(Identifier universeId, Identifier typeId, BlockNodePacketDecoder decoder) {
        GraphLibClientImpl.DECODERS.computeIfAbsent(universeId, _id -> new HashMap<>()).put(typeId, decoder);
    }

    /**
     * Registers a {@link BlockNodeRenderer} in the given universe for the given block node render id.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     *
     * @param universeId the universe this renderer is registered under.
     * @param renderId   the render id this renderer is to be registered for.
     * @param clazz      the class of the {@link ClientBlockNode} that this renderer expects.
     * @param renderer   the renderer.
     * @param <N>        the type of the {@link ClientBlockNode} that this renderer expects.
     */
    public static <N extends ClientBlockNode> void registerRenderer(Identifier universeId, Identifier renderId,
                                                                    Class<N> clazz, BlockNodeRenderer<N> renderer) {
        GraphLibClientImpl.RENDERERS.computeIfAbsent(universeId, _id -> new HashMap<>())
            .put(renderId, new BlockNodeRendererHolder<>(clazz, renderer));
    }

    /**
     * Registers a {@link BlockNodeRenderer} for the given block node render id for all universes.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     * <p>
     * Renderers for specific universes take priority over this. It is advised to only register for the universe(s) you
     * expect your renderer to be used in.
     *
     * @param renderId the render id this renderer is to be registered for.
     * @param clazz    the class of the {@link ClientBlockNode} that this renderer expects.
     * @param renderer the renderer.
     * @param <N>      the type of the {@link ClientBlockNode} that this renderer expects.
     */
    public static <N extends ClientBlockNode> void registerRendererForAllUniverses(Identifier renderId, Class<N> clazz,
                                                                                   BlockNodeRenderer<N> renderer) {
        GraphLibClientImpl.ALL_UNIVERSE_RENDERERS.put(renderId, new BlockNodeRendererHolder<>(clazz, renderer));
    }
}
