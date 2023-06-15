package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Creates new graph entities.
 */
@FunctionalInterface
public interface GraphEntityFactory {
    /**
     * Creates a new graph entity.
     *
     * @param ctx the graph entity context for this graph entity.
     * @return a newly created graph entity.
     */
    @NotNull GraphEntity<?> createNew(@NotNull GraphEntityContext ctx);
}
