package com.kneelawk.graphlib.v3.api.datastructure;

import com.kneelawk.graphlib.v3.api.pos.NodePos;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Link in a general purpose graph data structure.
 * <p>
 * This link is non-directional. A link from node 'A' to node 'B' is equal to a link from node 'B' to node 'A'. This is
 * upheld in this type's {@link #equals(Object)} and {@link #hashCode()}.
 * <p>
 * If a link needs to be directional, then its data should reference one of the nodes, making it asymmetrical.
 * Referencing a node can be done with a {@link NodePos}.
 *
 * @param <K>  the type of key used to index each node.
 * @param <V>  the type of data contained in each node.
 * @param <LK> the type of link key that determines whether two links between the same nodes are unique.
 * @param <LV> the type of data contained in each link.
 */
public final class KeyedLink<K, V, LK, LV> {
    private final KeyedNode<K, V, LK, LV> first;
    private final KeyedNode<K, V, LK, LV> second;
    private final Key<K, LK> key;
    private final LV value;

    /**
     * Creates a new keyed link.
     *
     * @param first  the first node in this link.
     * @param second the second node in this link.
     * @param key    the key that describes this link uniquely.
     * @param value  the value held within this link.
     */
    public KeyedLink(KeyedNode<K, V, LK, LV> first, KeyedNode<K, V, LK, LV> second, Key<K, LK> key, LV value) {
        this.first = first;
        this.second = second;
        this.key = key;
        this.value = value;

        if (!key.contains(first.key()) || !key.contains(second.key()))
            throw new IllegalArgumentException("The link key must contain the keys of the link's two nodes");
    }

    /**
     * {@return the first node in this link}
     */
    public KeyedNode<K, V, LK, LV> first() {
        return first;
    }

    /**
     * {@return the second node in this link}
     */
    public KeyedNode<K, V, LK, LV> second() {
        return second;
    }

    /**
     * {@return this link's key}
     */
    public Key<K, LK> key() {
        return key;
    }

    /**
     * {@return this link's value}
     */
    public LV value() {
        return value;
    }

    /**
     * Checks to see if the given node is either of the two nodes in this link.
     *
     * @param node the node to check.
     * @return <code>true</code> if the given node is either of the nodes in this link.
     */
    public boolean contains(KeyedNode<K, V, LK, LV> node) {
        return first.key().equals(node.key()) || second.key().equals(node.key());
    }

    /**
     * Checks to see if the given key is for either of the two nodes in this link.
     *
     * @param key the key to check.
     * @return <code>true</code> if a node with the given key is wither of the nodes in this link.
     */
    public boolean contains(K key) {
        return first.key().equals(key) || second.key().equals(key);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the link from.
     * @return the node at the other end of the link from the given node.
     */
    public KeyedNode<K, V, LK, LV> other(KeyedNode<K, V, LK, LV> node) {
        if (first.key().equals(node.key())) {
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
    public KeyedNode<K, V, LK, LV> other(K key) {
        if (first.key().equals(key)) {
            return second;
        } else {
            return first;
        }
    }

    /**
     * Establishes this link between this link's two nodes.
     *
     * @return {@code true} if a link was successfully established, {@code} false if a link already existed with this link's key.
     */
    public boolean link() {
        return first.onLink(this) == null & second.onLink(this) == null;
    }

    /**
     * Removes this link from the two nodes it is attached between.
     *
     * @return {@code true} if this link was connecting the two nodes to begin with.
     */
    public boolean unlink() {
        return first.onUnlink(key) == this & second.onUnlink(key) == this;
    }

    /**
     * {@return whether this link is connecting its two nodes}
     */
    public boolean isLinked() {
        return first.connections().get(key) == this && second.connections().get(key) == this;
    }

    @Override
    public String toString() {
        return "link(" + first + " <-> " + second + ", " + key + ')';
    }

    /**
     * Key used to index links.
     *
     * @param first  the unique key of the first node.
     * @param second the unique key of the second node.
     * @param key    the key that makes this link unique out of all the links between the same two nodes.
     * @param <N>    the type of node key.
     * @param <L>    the type of link key.
     */
    public record Key<N, L>(N first, N second, L key) {
        /**
         * Gets whether this link key contains the given node key.
         *
         * @param nodeKey the node key to find.
         * @return {@code true} if this link key contains the given node key.
         */
        public boolean contains(N nodeKey) {
            return first.equals(nodeKey) || second.equals(nodeKey);
        }

        /**
         * Gets the node key opposite the given node key.
         *
         * @param nodeKey the node key to get the opposite end of the link from.
         * @return the node key at the other end of the link.
         */
        public N other(N nodeKey) {
            if (first.equals(nodeKey)) {
                return second;
            } else {
                return first;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key<?, ?> linkKey = (Key<?, ?>) o;

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
    }
}
