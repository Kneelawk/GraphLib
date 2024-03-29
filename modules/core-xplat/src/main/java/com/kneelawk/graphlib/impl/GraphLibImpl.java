package com.kneelawk.graphlib.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class GraphLibImpl {
    private GraphLibImpl() {
    }

    public static final Map<Identifier, GraphUniverseImpl> UNIVERSE = new LinkedHashMap<>();

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandBuildContext context) {
        GraphLibCommand.register(dispatcher, context);
    }

    public static void register(GraphUniverseImpl universe) {
        if (UNIVERSE.containsKey(universe.getId())) throw new IllegalArgumentException(
            "A graph universe is already registered with the key: " + universe.getId());

        UNIVERSE.put(universe.getId(), universe);
    }
}
