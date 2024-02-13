package com.kneelawk.graphlib.impl;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;

public final class GraphLibImpl {
    private GraphLibImpl() {
    }

    public static final Identifier UNIVERSE_IDENTIFIER = Constants.id("universe");
    public static final RegistryKey<Registry<GraphUniverseImpl>> UNIVERSE_KEY =
        RegistryKey.ofRegistry(UNIVERSE_IDENTIFIER);

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandBuildContext context) {
        GraphLibCommand.register(dispatcher, context);
    }

    public static void register(GraphUniverseImpl universe) {
        Registry.register(GraphLibPlatform.INSTANCE.getUniverseRegistry(), universe.getId(), universe);
    }
}
