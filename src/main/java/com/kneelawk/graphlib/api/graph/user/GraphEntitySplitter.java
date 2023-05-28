package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Splits a new graph entity off of an existing graph entity.
 *
 * @param <G> the type of graph this splitter handles.
 */
@FunctionalInterface
public interface GraphEntitySplitter<G extends GraphEntity<G>> {
    /**
     * Splits a new graph entity off of an existing graph entity.
     *
     * @param original      the original graph entity.
     * @param originalGraph the graph of the original graph entity.
     * @param ctx           the graph entity context of the new graph entity. Note: this contains a reference to the new graph.
     * @return a newly created graph entity split off of the original graph entity.
     */
    @NotNull G splitNew(@NotNull G original, @NotNull BlockGraph originalGraph, @NotNull GraphEntityContext ctx);
}
