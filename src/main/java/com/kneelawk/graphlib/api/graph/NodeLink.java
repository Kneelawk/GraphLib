package com.kneelawk.graphlib.api.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.HalfLink;

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
     * Gets the key of this link.
     *
     * @return the key of this link.
     */
    @NotNull LinkKey getKey();

    /**
     * Checks whether either node is the given node.
     *
     * @param node the node to check against.
     * @return <code>true</code> if either node matches the given node.
     */
    default boolean contains(@NotNull NodeHolder<BlockNode> node) {
        return Objects.equals(getFirst(), node) || Objects.equals(getSecond(), node);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the connection from.
     * @return the node at the other end of the connection from the given node.
     */
    default @NotNull NodeHolder<BlockNode> other(@NotNull NodeHolder<BlockNode> node) {
        NodeHolder<BlockNode> first = getFirst();
        if (Objects.equals(first, node)) {
            return getSecond();
        } else {
            return first;
        }
    }

    /**
     * Represents this node link from the perspective of one of its nodes.
     *
     * @param perspective the node whose perspective from which this link is to be represented.
     * @return a keyed-node representing this link from the perspective of the given node.
     */
    default @NotNull HalfLink toHalfLink(@NotNull NodeHolder<BlockNode> perspective) {
        return new HalfLink(getKey(), other(perspective));
    }
}
