package com.kneelawk.graphlib.v3.api.datastructure;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Node in a general purpose graph data structure.
 *
 * @param <K>  the key of this node.
 * @param <V>  the value contained within this node.
 * @param <LK> the type of link data contained in links between nodes.
 * @param <LV> the type of data contained within each link.
 */
public final class KeyedNode<K, V, LK, LV> {
    private final K key;
    private final V value;
    private final Map<KeyedLink.Key<K, LK>, KeyedLink<K, V, LK, LV>> connections;

    /**
     * Constructs a new node containing the given data.
     *
     * @param key   the key that is used to find this node.
     * @param value the data for this node to contain.
     */
    public KeyedNode(K key, V value) {
        this.key = key;
        this.value = value;
        this.connections = new Object2ObjectLinkedOpenHashMap<>();
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
     * Gets this node's value.
     *
     * @return this node's value.
     */
    public V value() {
        return value;
    }

    /**
     * Gets this node's connections.
     * <p>
     * Care must be taken when mutating this map. It is best to use the methods provided by {@link KeyedNode} and
     * {@link KeyedLink} instead.
     *
     * @return this node's connections.
     */
    public Map<KeyedLink.Key<K, LK>, KeyedLink<K, V, LK, LV>> connections() {
        return connections;
    }

    /**
     * Unlinks the link attaching this node to the other node with the given key.
     *
     * @param otherKey the key of the other node the link is attached to.
     * @param linkKey  the key of the link itself that makes it unique from other links connecting to the other node.
     * @return the unlinked link.
     */
    public @Nullable KeyedLink<K, V, LK, LV> unlink(K otherKey, LK linkKey) {
        return unlink(new KeyedLink.Key<>(key, otherKey, linkKey));
    }

    /**
     * Unlinks a link attached to this node with the given key.
     *
     * @param key the key of the link to unlink.
     * @return the unlinked link if present.
     */
    public @Nullable KeyedLink<K, V, LK, LV> unlink(KeyedLink.Key<K, LK> key) {
        KeyedLink<K, V, LK, LV> link = connections.get(key);
        if (link == null) return null;
        link.unlink();
        return link;
    }

    /**
     * Called when another node is added to the graph.
     *
     * @param other the other node added to the graph.
     */
    void onAdded(KeyedNode<K, V, LK, LV> other) {
    }

    /**
     * Called when another node is removed from the graph so that this node can remove it from its connections.
     *
     * @param other the other node removed from the graph.
     */
    void onRemoved(K other) {
        connections.keySet().removeIf(link -> link.contains(other));
    }

    /**
     * Adds the given link as a connection this node has, if this node does not already have a link with the given key.
     *
     * @param link the link between this node and another node.
     * @return the current value associated with the link's key, if any.
     */
    @Nullable KeyedLink<K, V, LK, LV> onLink(KeyedLink<K, V, LK, LV> link) {
        return connections.putIfAbsent(link.key(), link);
    }

    /**
     * Removes the given link as a connection this node has.
     *
     * @param link the link to remove.
     * @return the keyed link associated with the given key.
     */
    @Nullable KeyedLink<K, V, LK, LV> onUnlink(KeyedLink.Key<K, LK> link) {
        return connections.remove(link);
    }

    @Override
    public String toString() {
        return "node[" + key + ']';
    }
}
