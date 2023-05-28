package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.GraphWorld;

public record SimpleGraphEntityContext(ServerWorld blockWorld, SimpleGraphWorld graphWorld, BlockGraph graph) implements GraphEntityContext {
    @Override
    public void markDirty() {
        graphWorld.markDirty(graph.getId());
    }

    @Override
    public @NotNull ServerWorld getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphWorld getGraphWorld() {
        return graphWorld;
    }

    @Override
    public @NotNull BlockGraph getGraph() {
        return graph;
    }
}
