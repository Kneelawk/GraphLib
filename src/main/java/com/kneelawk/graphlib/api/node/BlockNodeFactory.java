package com.kneelawk.graphlib.api.node;

import com.kneelawk.graphlib.api.graph.NodeContext;

/**
 * Creates a new block node with the given context.
 */
public interface BlockNodeFactory {
    /**
     * Construct a new block node with the given context.
     *
     * @param ctx the context to give to the block node.
     * @return the newly created block node.
     */
    BlockNode createNew(NodeContext ctx);
}
