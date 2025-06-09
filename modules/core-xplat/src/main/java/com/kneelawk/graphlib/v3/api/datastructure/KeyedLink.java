package com.kneelawk.graphlib.v3.api.datastructure;

import java.util.Objects;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Link in a general purpose graph data structure.
 * <p>
 * This link is non-directional. A link from node 'A' to node 'B' is equal to a link from node 'B' to node 'A'. This is
 * upheld in this type's {@link #equals(Object)} and {@link #hashCode()}.
 * <p>
 * If a link needs to be directional, then its data should reference one of the nodes, making it asymmetrical.
 * Referencing a node can be done with a {@link com.kneelawk.graphlib.v3.api.pos.NodePos}.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param key    the key that makes this link unique out of all the links between the same two nodes.
 * @param <K>    the type of key used to index each node.
 * @param <L>    the type of link data contained in this link.
 */
public record KeyedLink<K, L>(KeyedNode<K, L> first, KeyedNode<K, L> second, L key) {
    /**
     * Checks to see if the given node is either of the two nodes in this link.
     *
     * @param node the node to check.
     * @return <code>true</code> if the given node is either of the nodes in this link.
     */
    public boolean contains(KeyedNode<K, L> node) {
        return Objects.equals(first, node) || Objects.equals(second, node);
    }

    /**
     * Checks to see if the given key is for either of the two nodes in this link.
     *
     * @param key the key to check.
     * @return <code>true</code> if a node with the given key is wither of the nodes in this link.
     */
    public boolean contains(K key) {
        return Objects.equals(first.key(), key) || Objects.equals(second.key(), key);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the link from.
     * @return the node at the other end of the link from the given node.
     */
    public KeyedNode<K, L> other(KeyedNode<K, L> node) {
        if (Objects.equals(first, node)) {
            return second;
        } else {
            return first;
        }
    }

    /**
     * Gets the node opposite the node with the given key.
     *
     * @param key the key of the node to get the other end of hte link from.
     * @return the node at the other end of the link from the node with the given key.
     */
    public KeyedNode<K, L> other(K key) {
        if (Objects.equals(first.key(), key)) {
            return second;
        } else {
            return first;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyedLink<?, ?> linkKey = (KeyedLink<?, ?>) o;

        if (!key.equals(linkKey.key)) return false;

        // note: hashCode and equals are symmetrical regardless of node order
        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        // note: hashCode and equals are symmetrical regardless of node order
        int result = first.hashCode();
        result = result ^ second.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "link(" + first + " <-> " + second + ", " + key + ')';
    }
}
