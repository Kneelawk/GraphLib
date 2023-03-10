package com.kneelawk.graphlib.impl;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Lifecycle;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.GraphLib;
import com.kneelawk.graphlib.api.v1.event.PreBuildUniverse;
import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class GraphLibImpl {
    private GraphLibImpl() {
    }

    private static final Identifier UNIVERSE_IDENTIFIER = Constants.id("universe");
    public static final RegistryKey<Registry<GraphUniverseImpl>> UNIVERSE_KEY =
        RegistryKey.ofRegistry(UNIVERSE_IDENTIFIER);
    private static final Identifier BLOCK_NODE_DECODER_IDENTIFIER = Constants.id("block_node_decoder");
    public static final RegistryKey<Registry<BlockNodeDecoder>> BLOCK_NODE_DECODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_DECODER_IDENTIFIER);
    private static final Identifier BLOCK_NODE_PACKET_ENDODER_IDENTIFIER = Constants.id("block_node_packet_encoder");
    public static final RegistryKey<Registry<BlockNodePacketEncoderHolder<?>>> BLOCK_NODE_PACKET_ENCODER_KEY =
        RegistryKey.ofRegistry(BLOCK_NODE_PACKET_ENDODER_IDENTIFIER);
    public static final List<BlockNodeDiscoverer> BLOCK_NODE_DISCOVERERS = new ArrayList<>();

    public static final Registry<GraphUniverseImpl> UNIVERSE =
        new SimpleRegistry<>(UNIVERSE_KEY, Lifecycle.experimental(), false);

    private static final List<EntrypointContainer<PreBuildUniverse>> PRE_BUILD_LISTENERS =
        FabricLoader.getInstance().getEntrypointContainers(Constants.UNIVERSE_PRE_BUILD, PreBuildUniverse.class);

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, UNIVERSE_IDENTIFIER, UNIVERSE);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_DECODER_IDENTIFIER,
            GraphLib.BLOCK_NODE_DECODER);
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, BLOCK_NODE_PACKET_ENDODER_IDENTIFIER,
            GraphLib.BLOCK_NODE_PACKET_ENCODER);
    }

    static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext context) {
        GraphLibCommand.register(dispatcher, context);
    }

    public static void register(GraphUniverseImpl universe) {
        Registry.register(UNIVERSE, universe.getId(), universe);
    }

    public static void preBuild(Identifier universeId, GraphUniverse.Builder builder) {
        for (EntrypointContainer<PreBuildUniverse> container : PRE_BUILD_LISTENERS) {
            container.getEntrypoint().preBuild(universeId, builder);
        }
    }
}
