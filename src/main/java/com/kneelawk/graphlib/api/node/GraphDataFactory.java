package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;

/**
 * A factory for graph data.
 *
 * @param <G> the type of graph data this factory creates.
 */
@FunctionalInterface
public interface GraphDataFactory<G extends GraphData<G>> {
    /**
     * Creates new empty graph data.
     *
     * @param markDirty  call this when graph data has changed, to let GraphLib know it needs to save the graph.
     * @param blockWorld the block world that this data's graph is in.
     * @param graphWorld the graph world that this data's graph is in.
     * @param graph      the block graph that this data will be a part of.
     * @return the newly created, empty graph data.
     */
    @NotNull G createNew(@NotNull Runnable markDirty, @NotNull ServerWorld blockWorld, @NotNull GraphWorld graphWorld,
                         @NotNull BlockGraph graph);
}
