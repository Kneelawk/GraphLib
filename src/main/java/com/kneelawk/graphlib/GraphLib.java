package com.kneelawk.graphlib;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.command.GraphLibCommand;
import com.kneelawk.graphlib.graph.BlockGraphController;
import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeDecoder;
import com.kneelawk.graphlib.graph.BlockNodeDiscoverer;
import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import com.kneelawk.graphlib.mixin.api.StorageHelper;
import com.kneelawk.graphlib.net.BlockNodePacketEncoderHolder;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, obtaining a
 * {@link BlockGraphController}, or registering {@link BlockNodeDecoder}s and {@link BlockNodeDiscoverer}s.
 */
public final class GraphLib {
    private static final Identifier BLOCK_NODE_DECODER_IDENTIFIER = Constants.id("block_node_decoder");
    private static final RegistryKey<Registry<BlockNodeDecoder>> BLOCK_NODE_DECODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_DECODER_IDENTIFIER);
    private static final Identifier BLOCK_NODE_PACKET_ENDODER_IDENTIFIER = Constants.id("block_node_packet_encoder");
    private static final RegistryKey<Registry<BlockNodePacketEncoderHolder<?>>> BLOCK_NODE_PACKET_ENCODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_PACKET_ENDODER_IDENTIFIER);
    private static final List<BlockNodeDiscoverer> BLOCK_NODE_DISCOVERERS = new ArrayList<>();

    /**
     * Registry of {@link BlockNodeDecoder}s for block-node type ids.
     */
    public static final Registry<BlockNodeDecoder> BLOCK_NODE_DECODER =
        new SimpleRegistry<>(BLOCK_NODE_DECODER_KEY, Lifecycle.experimental());

    /**
     * Registry of {@link BlockNodePacketEncoderHolder}s for encoding nodes to send to the client for debug rendering.
     */
    public static final Registry<BlockNodePacketEncoderHolder<?>> BLOCK_NODE_PACKET_ENCODER =
        new SimpleRegistry<>(BLOCK_NODE_PACKET_ENCODER_KEY, Lifecycle.experimental());

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
        BLOCK_NODE_DISCOVERERS.add(discoverer);
    }

    /**
     * Gets a set of all the {@link BlockNode}s a block <b>should</b> have.
     *
     * @param world the world to check in.
     * @param pos   the position to check at.
     * @return a set of all the {@link BlockNode}s that should be at that position.
     */
    public static @NotNull Set<BlockNode> getNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos) {
        return BLOCK_NODE_DISCOVERERS.stream().flatMap(discoverer -> discoverer.getNodesInBlock(world, pos).stream())
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

    // ---- Internal Stuff ---- //

    private GraphLib() {
    }

    static @NotNull SimpleBlockGraphController getSimpleController(@NotNull ServerWorld world) {
        return StorageHelper.getController(world);
    }

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_DECODER_IDENTIFIER,
            BLOCK_NODE_DECODER);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_PACKET_ENDODER_IDENTIFIER,
            BLOCK_NODE_PACKET_ENCODER);
    }

    static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        GraphLibCommand.register(dispatcher);
    }
}
