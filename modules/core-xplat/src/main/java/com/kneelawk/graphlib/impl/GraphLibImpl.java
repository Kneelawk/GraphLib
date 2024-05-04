package com.kneelawk.graphlib.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class GraphLibImpl {
    private GraphLibImpl() {
    }

    public static final Map<ResourceLocation, GraphUniverseImpl> UNIVERSE = new LinkedHashMap<>();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                        CommandBuildContext context) {
        GraphLibCommand.register(dispatcher, context);
    }

    public static void register(GraphUniverseImpl universe) {
        if (UNIVERSE.containsKey(universe.getId())) throw new IllegalArgumentException(
            "A graph universe is already registered with the key: " + universe.getId());

        UNIVERSE.put(universe.getId(), universe);
    }
}
