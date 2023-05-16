package com.kneelawk.graphlib.api.util.graph;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

/**
 * Node in a general purpose graph data structure.
 * <p>
 * Translated from
 * <a href="https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt">2xsaiko's HCTM-Base Graph code</a>.
 *
 * @param <K> this node's key (comparable).
 * @param <V> this node's value (non-comparable).
 */
public final class Node<K, V> {
    private final K key;
    private final V value;
    private final Object2ObjectMap<K, Link<K, V>> connections;

    /**
     * Constructs a new node containing the given data.
     *
     * @param key   this node's key and the thing its equals and hash-code are based off of.
     * @param value this node's value.
     */
    public Node(@NotNull K key, @NotNull V value) {
        this.key = key;
        this.value = value;
        this.connections = new Object2ObjectLinkedOpenHashMap<>();
    }

    /**
     * Gets this node's key.
     *
     * @return this node's key.
     */
    public @NotNull K key() {
        return key;
    }

    /**
     * Gets this node's value.
     *
     * @return this node's value.
     */
    public @NotNull V value() {
        return value;
    }

    /**
     * Gets this node's connections.
     *
     * @return this node's connections.
     */
    public @NotNull Collection<Link<K, V>> connections() {
        return connections.values();
    }

    /**
     * Called when another node is added to the graph.
     *
     * @param other the other node added to the graph.
     */
    void nodeAdded(@NotNull Node<K, V> other) {
    }

    /**
     * Called when another node is removed from the graph so that this node can remove it from its connections.
     *
     * @param otherKey the key of the other node removed from the graph.
     */
    void nodeRemoved(@NotNull K otherKey) {
        connections.remove(otherKey);
    }

    /**
     * Adds the given link as a connection this node has.
     *
     * @param link the link between this node and another node.
     */
    void addLink(@NotNull Link<K, V> link) {
        connections.put(link.other(this).key, link);
    }

    /**
     * Removes the given link as a connection this node has.
     * <p>
     * Note: links are technically directional and must be removed twice, once in each direction, to make sure the link
     * has actually been removed.
     *
     * @param link the link to remove.
     */
    void removeLink(@NotNull Link<K, V> link) {
        connections.remove(link.other(this).key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?, ?> node = (Node<?, ?>) o;
        return Objects.equals(key, node.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Node[" +
            "key=" + key +
            ", value=" + value +
            ']';
    }
}
