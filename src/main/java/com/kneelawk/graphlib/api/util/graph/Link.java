package com.kneelawk.graphlib.api.util.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Link in a general purpose graph data structure.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param <T>    the type of data contained in each node.
 */
public record Link<T>(@NotNull Node<T> first, @NotNull Node<T> second) {
    /**
     * Checks to see if the given node is either of the two nodes in this link.
     *
     * @param node the node to check.
     * @return <code>true</code> if the given node is either of the nodes in this link.
     */
    public boolean contains(@NotNull Node<T> node) {
        return Objects.equals(first, node) || Objects.equals(second, node);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the link from.
     * @return the node at the other end of the link from the given node.
     */
    public @NotNull Node<T> other(@NotNull Node<T> node) {
        if (Objects.equals(first, node)) {
            return second;
        } else {
            return first;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link<?> linkKey = (Link<?>) o;

        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = result ^ second.hashCode();
        return result;
    }
}
