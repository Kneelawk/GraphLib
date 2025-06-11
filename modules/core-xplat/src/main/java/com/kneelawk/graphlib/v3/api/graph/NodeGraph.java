package com.kneelawk.graphlib.v3.api.graph;

import com.kneelawk.graphlib.v3.api.datastructure.MappedGraph;
import com.kneelawk.graphlib.v3.api.graph.user.LinkKey;
import com.kneelawk.graphlib.v3.api.pos.NodePos;

/**
 * Holds and manages a set of graph nodes.
 */
public final class NodeGraph {
    private final MappedGraph<NodePos, LinkKey> graph = new MappedGraph<>();
}
