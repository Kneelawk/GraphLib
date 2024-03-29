package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Creates link keys from context.
 */
public interface LinkKeyFactory {
    /**
     * Create a link key from the given context and other node.
     *
     * @param self  the node creating the link.
     * @param other the node the link is being created to.
     * @return a new link key for the given context.
     */
    @NotNull LinkKey createLinkKey(@NotNull NodeHolder<BlockNode> self, NodeHolder<BlockNode> other);
}
