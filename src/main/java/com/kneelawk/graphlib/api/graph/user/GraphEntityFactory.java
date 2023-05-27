package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Creates new graph entities.
 *
 * @param <G> the type of graph entity this factory creates.
 */
@FunctionalInterface
public interface GraphEntityFactory<G extends GraphEntity<G>> {
    /**
     * Creates a new graph entity.
     *
     * @param ctx the graph entity context for this graph entity.
     * @return a newly created graph entity.
     */
    @NotNull G createNew(@NotNull GraphEntityContext ctx);
}
