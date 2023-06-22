package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

/**
 * Creates new graph entities.
 */
@FunctionalInterface
public interface GraphEntityFactory {
    /**
     * Creates a new graph entity.
     *
     * @return a newly created graph entity.
     */
    @NotNull GraphEntity<?> createNew();
}
