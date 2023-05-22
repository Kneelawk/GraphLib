package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.NodeContext;

/**
 * Creates a new block node with the given context.
 */
public interface BlockNodeFactory {
    /**
     * Construct a new block node with the given context.
     *
     * @param ctx the context to give to the block node.
     * @return the newly created block node, or <code>null</code> if a node could not be created.
     */
    @Nullable BlockNode createNew(@NotNull NodeContext ctx);
}
