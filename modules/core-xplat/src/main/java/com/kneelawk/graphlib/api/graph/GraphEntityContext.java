package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.World;

/**
 * Context for a graph entity.
 */
public interface GraphEntityContext {
    /**
     * Marks this entity's graph as dirty and in need of saving.
     */
    void markDirty();

    /**
     * Gets the block world that this graph entity exists within.
     *
     * @return the block world that this graph entity exists within.
     */
    @NotNull World getBlockWorld();

    /**
     * Gets the graph world that this graph entity exists within.
     *
     * @return the graph world that this graph entity exists within.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the graph that this graph entity is associated with.
     *
     * @return the graph that this graph entity is associated with.
     */
    @NotNull BlockGraph getGraph();
}
