package com.kneelawk.graphlib.v3.api.graph.user;

import com.mojang.serialization.MapCodec;

/**
 * Interface that all graph nodes should implement.
 * <p>
 * A graph node is a piece of immutable data that sits in a graph and can be used to allow utilities to determine which
 * things are connected to which and how.
 */
public interface GraphNode {
    // TODO: universe lookups
    /**
     * {@link GraphNode} map codec.
     */
    MapCodec<GraphNode> MAP_CODEC = MapCodec.unit(() -> {throw new AssertionError("Stub");});
}
