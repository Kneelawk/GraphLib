package com.kneelawk.graphlib.api.util.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Link in a general purpose graph data structure.
 * <p>
 * <b>Note that this is <u>not</u> direction dependent.</b> Links' {@link #equals(Object)} and {@link #hashCode()}
 * functions act the same no matter which node is first and which is second.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param <K>    the key contained in each node.
 * @param <V>    the value contained in each node.
 */
public record Link<K, V>(@NotNull Node<K, V> first, @NotNull Node<K, V> second) {
    /**
     * Checks to see if the given node key is for either of the two nodes in this link.
     *
     * @param key the key to check.
     * @return <code>true</code> if the given node key is for either of the nodes in this link.
     */
    public boolean contains(@NotNull K key) {
        return Objects.equals(first.key(), key) || Objects.equals(second.key(), key);
    }

    /**
     * Gets the node opposite the node with the given key.
     *
     * @param key the key of the node to get the other end of the link from.
     * @return the node at the other end of the link from the node with the given key.
     */
    public @NotNull Node<K, V> other(@NotNull K key) {
        if (Objects.equals(first.key(), key)) {
            return second;
        } else {
            return first;
        }
    }

    /**
     * Removes this link from both of its nodes.
     */
    public void unlink() {
        first.removeLink(second.key());
        second.removeLink(first.key());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link<?, ?> linkKey = (Link<?, ?>) o;

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
