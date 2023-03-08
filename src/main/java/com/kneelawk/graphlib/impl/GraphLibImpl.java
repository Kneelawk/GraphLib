package com.kneelawk.graphlib.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.GraphLib;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class GraphLibImpl {
    private GraphLibImpl() {
    }

    private static final Identifier BLOCK_NODE_DECODER_IDENTIFIER = Constants.id("block_node_decoder");
    public static final RegistryKey<Registry<BlockNodeDecoder>> BLOCK_NODE_DECODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_DECODER_IDENTIFIER);
    private static final Identifier BLOCK_NODE_PACKET_ENDODER_IDENTIFIER = Constants.id("block_node_packet_encoder");
    public static final RegistryKey<Registry<BlockNodePacketEncoderHolder<?>>> BLOCK_NODE_PACKET_ENCODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_PACKET_ENDODER_IDENTIFIER);
    public static final List<BlockNodeDiscoverer> BLOCK_NODE_DISCOVERERS = new ArrayList<>();

    public static final Map<Identifier, GraphUniverseImpl> UNIVERSES = new Object2ObjectLinkedOpenHashMap<>();

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_DECODER_IDENTIFIER,
            GraphLib.BLOCK_NODE_DECODER);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_PACKET_ENDODER_IDENTIFIER,
            GraphLib.BLOCK_NODE_PACKET_ENCODER);
    }

    static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        GraphLibCommand.register(dispatcher);
    }

    public static void register(GraphUniverseImpl universe) {
        if (UNIVERSES.containsKey(universe.getId())) {
            throw new IllegalArgumentException("Attempted to register a universe with a name that is already used. Name: " + universe.getId());
        }

        UNIVERSES.put(universe.getId(), universe);
    }
}
