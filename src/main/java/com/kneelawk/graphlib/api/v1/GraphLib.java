package com.kneelawk.graphlib.api.v1;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.graph.GraphWorld;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, obtaining a
 * {@link GraphWorld}, or registering {@link BlockNodeDecoder}s and {@link BlockNodeDiscoverer}s.
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
     * The universe representing the data managed by pre-1.0 versions of GraphLib.
     *
     * @deprecated It is recommended for mods to build and use their own universes with {@link GraphUniverse#builder()}.
     */
    @Deprecated
    public static final GraphUniverse LEGACY_UNIVERSE =
        GraphUniverse.builder().build(Constants.id(Constants.GRAPHDATA_DIRNAME));

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
     * Gets a registered graph universe by its id.
     *
     * @param universeId the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static @NotNull GraphUniverse getUniverse(Identifier universeId) {
        GraphUniverseImpl graphUniverse = GraphLibImpl.UNIVERSE.get(universeId);
        if (graphUniverse == null) {
            throw new IllegalArgumentException("No universe exists with the name " + universeId);
        }

        return graphUniverse;
    }
}
