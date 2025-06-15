package com.kneelawk.graphlib.v3.api.graph;

/**
 * Mutable access to all graphs in a save (across all levels).
 */
public interface GraphWorld extends GraphView {
    @Override
    default boolean isClient() {
        return false;
    }
}
