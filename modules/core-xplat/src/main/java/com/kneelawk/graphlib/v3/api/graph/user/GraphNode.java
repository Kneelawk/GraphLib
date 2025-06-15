package com.kneelawk.graphlib.v3.api.graph.user;

import com.mojang.serialization.MapCodec;

import com.kneelawk.graphlib.v3.api.graph.GraphUniverse;

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
    /**
     * {@link GraphNode} map codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    MapCodec<GraphNode> MAP_CODEC = GraphNodeType.REF_CODEC.dispatchMap(GraphNode::getType, GraphNodeType::getCodec);

    /**
     * {@link #MAP_CODEC} with universe attached.
     *
     * @param universe the universe to attach.
     * @return the map codec.
     */
    static MapCodec<GraphNode> mapCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingMapCodec(universe, MAP_CODEC);
    }

    /**
     * Gets this graph node's type.
     *
     * @return the type of this graph node.
     */
    GraphNodeType getType();
}
