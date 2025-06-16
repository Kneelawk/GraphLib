package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.v3.api.pos.NodePos;

/**
 * An immutable view of all graphs in a save (across all levels).
 */
public interface GraphView {
    /**
     * {@return whether this graph view is on the logical client}
     */
    default boolean isClient() {
        return true;
    }

    /**
     * {@return the graph universe associated with this graph view}
     */
    GraphUniverse getUniverse();

    /**
     * Gets the node holder for the given node pos, if a node actually exists at that pos.
     *
     * @param pos the node pos to get the node holder at.
     * @return the node holder at the given pos.
     */
    @Nullable NodeHolder getNode(NodePos pos);
}
