package com.kneelawk.graphlib.impl;

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
import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.UniverseModifierRegistryImpl;

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

    public static final Registry<GraphUniverseImpl> UNIVERSE =
        new SimpleRegistry<>(UNIVERSE_KEY, Lifecycle.experimental(), false);

    private static final UniverseModifierRegistryImpl UNIVERSE_MODIFIER_REGISTRY = UniverseModifierRegistryImpl.setup();

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
        UNIVERSE_MODIFIER_REGISTRY.preBuild(universeId, builder);
    }
}
