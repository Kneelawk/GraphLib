package com.kneelawk.graphlib.v3.api.datastructure;

import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Node in a general purpose graph data structure.
 *
 * @param <K> the key of this node.
 * @param <L> the type of link data contained in links between nodes.
 */
public final class KeyedNode<K, L> {
    private final K key;
    private final Set<KeyedLink<K, L>> connections;

    /**
     * Constructs a new node containing the given data.
     *
     * @param key the data for this node to contain.
     */
    public KeyedNode(K key) {
        this.key = key;
        this.connections = new ObjectLinkedOpenHashSet<>();
    }

    /**
     * Gets this node's key.
     *
     * @return this node's key.
     */
    public K key() {
        return key;
    }

    /**
     * Gets this node's connections.
     *
     * @return this node's connections.
     */
    public Set<KeyedLink<K, L>> connections() {
        return connections;
    }

    /**
     * Called when another node is added to the graph.
     *
     * @param other the other node added to the graph.
     */
    public void onAdded(KeyedNode<K, L> other) {
    }

    /**
     * Called when another node is removed from the graph so that this node can remove it from its connections.
     *
     * @param other the other node removed from the graph.
     */
    public void onRemoved(KeyedNode<K, L> other) {
        connections.removeIf(link -> link.contains(other));
    }

    /**
     * Adds the given link as a connection this node has.
     *
     * @param link the link between this node and another node.
     * @return <code>true</code> if the link did not already exist.
     */
    public boolean onLink(KeyedLink<K, L> link) {
        return connections.add(link);
    }

    /**
     * Removes the given link as a connection this node has.
     * <p>
     * Note: links are technically directional and must be removed twice, once in each direction, to make sure the link
     * has actually been removed.
     *
     * @param link the link to remove.
     * @return <code>true</code> if the link existed before being removed.
     */
    public boolean onUnlink(KeyedLink<K, L> link) {
        return connections.remove(link);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        @SuppressWarnings("rawtypes") var that = (KeyedNode) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "node[" + key + ']';
    }
}
