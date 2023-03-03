package com.kneelawk.graphlib.api.v1;

import com.kneelawk.graphlib.api.v1.graph.BlockGraphController;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, obtaining a
 * {@link BlockGraphController}, or registering {@link BlockNodeDecoder}s and {@link BlockNodeDiscoverer}s.
 */
public final class GraphLib {
    private GraphLib() {
    }

    /**
     * Registry of {@link BlockNodeDecoder}s for block-node type ids.
     */
    public static final Registry<BlockNodeDecoder> BLOCK_NODE_DECODER =
        new SimpleRegistry<>(GraphLibImpl.BLOCK_NODE_DECODER_KEY, Lifecycle.experimental());

    /**
     * Registry of {@link BlockNodePacketEncoderHolder}s for encoding nodes to send to the client for debug rendering.
     */
    public static final Registry<BlockNodePacketEncoderHolder<?>> BLOCK_NODE_PACKET_ENCODER =
        new SimpleRegistry<>(GraphLibImpl.BLOCK_NODE_PACKET_ENCODER_KEY, Lifecycle.experimental());

    /**
     * Registers a {@link BlockNodeDiscoverer} for use in detecting the nodes in a given block position.
     * <p>
     * Nodes that are discovered here are usually created by the block or block-entity present at the given location.
     * These nodes are then compared, using their <code>hashCode()</code> and <code>equals()</code> functions, to the
     * nodes already in the controller's graphs at the given location and used to make adjustments if necessary.
     *
     * @param discoverer the discoverer used for detecting the nodes in a given block position.
     */
    public static void registerDiscoverer(@NotNull BlockNodeDiscoverer discoverer) {
        GraphLibImpl.BLOCK_NODE_DISCOVERERS.add(discoverer);
    }

    /**
     * Gets a set of all the {@link BlockNode}s a block <b>should</b> have.
     *
     * @param world the world to check in.
     * @param pos   the position to check at.
     * @return a set of all the {@link BlockNode}s that should be at that position.
     */
    public static @NotNull Set<BlockNode> getNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos) {
        return GraphLibImpl.BLOCK_NODE_DISCOVERERS.stream()
            .flatMap(discoverer -> discoverer.getNodesInBlock(world, pos).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Gets the {@link BlockGraphController} for the given {@link ServerWorld}.
     *
     * @param world the world whose BlockGraphController is to be obtained.
     * @return the BlockGraphController of the given world.
     */
    public static @NotNull BlockGraphController getController(@NotNull ServerWorld world) {
        return StorageHelper.getController(world);
    }
}
