package com.kneelawk.graphlib.api.client;

import java.util.HashMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.client.render.BlockNodeDebugRenderer;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.debug.DebugBlockNode;
import com.kneelawk.graphlib.impl.client.GraphLibClientImpl;
import com.kneelawk.graphlib.impl.client.debug.render.BlockNodeDebugRendererHolder;

/**
 * Graph Lib Client-Side Public API. This class contains static methods and fields for registering
 * {@link BlockNodeDebugPacketDecoder}s and renderers.
 */
@Environment(EnvType.CLIENT)
public final class GraphLibClient {
    private GraphLibClient() {
    }

    /**
     * Registers a {@link BlockNodeDebugPacketDecoder} in the given universe for the given block node type id.
     * <p>
     * Only register a decoder if you implement
     * {@link BlockNode#toDebugPacket(com.kneelawk.graphlib.api.graph.NodeHolder, PacketByteBuf)}
     * to provide custom node data to the client.
     *
     * @param universeId the universe this decoder is to be registered under.
     * @param typeId     the block node type id this decoder is to be registered for.
     * @param decoder    the decoder.
     */
    public static void registerDebugDecoder(Identifier universeId, Identifier typeId,
                                            BlockNodeDebugPacketDecoder decoder) {
        GraphLibClientImpl.DEBUG_DECODERS.computeIfAbsent(universeId, _id -> new HashMap<>()).put(typeId, decoder);
    }

    /**
     * Registers a {@link BlockNodeDebugRenderer} in the given universe for the given block node render id.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     *
     * @param universeId the universe this renderer is registered under.
     * @param renderId   the render id this renderer is to be registered for.
     * @param clazz      the class of the {@link DebugBlockNode} that this renderer expects.
     * @param renderer   the renderer.
     * @param <N>        the type of the {@link DebugBlockNode} that this renderer expects.
     */
    public static <N extends DebugBlockNode> void registerDebugRenderer(Identifier universeId, Identifier renderId,
                                                                        Class<N> clazz,
                                                                        BlockNodeDebugRenderer<N> renderer) {
        GraphLibClientImpl.DEBUG_RENDERERS.computeIfAbsent(universeId, _id -> new HashMap<>())
            .put(renderId, new BlockNodeDebugRendererHolder<>(clazz, renderer));
    }

    /**
     * Registers a {@link BlockNodeDebugRenderer} for the given block node render id for all universes.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     * <p>
     * Renderers for specific universes take priority over this. It is advised to only register for the universe(s) you
     * expect your renderer to be used in.
     *
     * @param renderId the render id this renderer is to be registered for.
     * @param clazz    the class of the {@link DebugBlockNode} that this renderer expects.
     * @param renderer the renderer.
     * @param <N>      the type of the {@link DebugBlockNode} that this renderer expects.
     */
    public static <N extends DebugBlockNode> void registerDebugRendererForAllUniverses(Identifier renderId,
                                                                                       Class<N> clazz,
                                                                                       BlockNodeDebugRenderer<N> renderer) {
        GraphLibClientImpl.ALL_UNIVERSE_DEBUG_RENDERERS.put(renderId,
            new BlockNodeDebugRendererHolder<>(clazz, renderer));
    }
}
