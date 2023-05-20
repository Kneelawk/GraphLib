package com.kneelawk.graphlib.api.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.PosNodeKey;

/**
 * Describes a connection between two nodes. May contain its own data, depending on implementation.
 */
public interface NodeLink {
    /**
     * Gets the first node in this connection.
     *
     * @return the first node in this connection.
     */
    @NotNull NodeHolder<BlockNode> getFirst();

    /**
     * Gets the second node in this connection.
     *
     * @return the second node in this connection.
     */
    @NotNull NodeHolder<BlockNode> getSecond();

    /**
     * Checks whether either node is the given node.
     *
     * @param node the node to check against.
     * @return <code>true</code> if either node matches the given node.
     */
    default boolean contains(@NotNull PosNodeKey node) {
        return Objects.equals(getFirst().toNodeKey(), node) || Objects.equals(getSecond().toNodeKey(), node);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the connection from.
     * @return the node at the other end of the connection from the given node.
     */
    default @NotNull NodeHolder<BlockNode> other(@NotNull PosNodeKey node) {
        NodeHolder<BlockNode> first = getFirst();
        if (Objects.equals(first.toNodeKey(), node)) {
            return getSecond();
        } else {
            return first;
        }
    }
}
