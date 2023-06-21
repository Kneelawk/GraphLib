package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.BlockGraph;

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
     * @param newGraph      the graph of the new graph entity.
     * @return a newly created graph entity split off of the original graph entity.
     */
    @NotNull GraphEntity<?> splitNew(@NotNull G original, @NotNull BlockGraph originalGraph,
                                     @NotNull BlockGraph newGraph);
}
