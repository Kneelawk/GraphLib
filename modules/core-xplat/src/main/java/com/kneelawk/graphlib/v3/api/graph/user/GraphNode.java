package com.kneelawk.graphlib.v3.api.graph.user;

import com.mojang.serialization.MapCodec;

/**
 * Interface that all graph nodes should implement.
 * <p>
 * A graph node is a piece of immutable data that sits in a graph and can be used to allow utilities to determine which
 * things are connected to which and how.
 * <p>
 * An important note for the way to treat graph nodes is that graph nodes are keys. They only hold enough information
 * to uniquely identify their node. In order to store arbitrary data in a node, please use a node entity.
 */
public interface GraphNode {
    // TODO: universe lookups
    /**
     * {@link GraphNode} map codec.
     */
    MapCodec<GraphNode> MAP_CODEC = MapCodec.unit(() -> {throw new AssertionError("Stub");});
}
