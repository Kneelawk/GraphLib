package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.GraphView;

public record SimpleGraphEntityContext(World blockWorld, SimpleGraphCollection graphWorld, BlockGraph graph) implements GraphEntityContext {
    @Override
    public void markDirty() {
        graphWorld.markDirty(graph.getId());
    }

    @Override
    public @NotNull World getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphView getGraphWorld() {
        return graphWorld;
    }

    @Override
    public @NotNull BlockGraph getGraph() {
        return graph;
    }
}
