package com.kneelawk.graphlib.api.v1.client;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;

import com.kneelawk.graphlib.api.v1.client.render.BlockNodeRendererHolder;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketDecoder;
import com.kneelawk.graphlib.impl.client.GraphLibClientImpl;

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
}
