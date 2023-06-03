package com.kneelawk.graphlib.api.util.graph;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Node in a general purpose graph data structure.
 *
 * @param <T> the type of data this node contains.
 * @param <L> the type of link data contained in links between nodes.
 */
public final class Node<T, L> {
    private final T data;
    private final Set<Link<T, L>> connections;

    /**
     * Constructs a new node containing the given data.
     *
     * @param data the data for this node to contain.
     */
    public Node(T data) {
        this.data = data;
        this.connections = new LinkedHashSet<>();
    }

    /**
     * Gets this node's data.
     *
     * @return this node's data.
     */
    public T data() {
        return data;
    }

    /**
     * Gets this node's connections.
     *
     * @return this node's connections.
     */
    public @NotNull Set<Link<T, L>> connections() {
        return connections;
    }

    /**
     * Called when another node is added to the graph.
     *
     * @param other the other node added to the graph.
     */
    public void onAdded(@NotNull Node<T, L> other) {
    }

    /**
     * Called when another node is removed from the graph so that this node can remove it from its connections.
     *
     * @param other the other node removed from the graph.
     */
    public void onRemoved(@NotNull Node<T, L> other) {
        connections.removeIf(link -> link.contains(other));
    }

    /**
     * Adds the given link as a connection this node has.
     *
     * @param link the link between this node and another node.
     * @return <code>true</code> if the link did not already exist.
     */
    public boolean onLink(@NotNull Link<T, L> link) {
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
    public boolean onUnlink(@NotNull Link<T, L> link) {
        return connections.remove(link);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        @SuppressWarnings("rawtypes") var that = (Node) obj;
        return Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "Node[" +
            "node=" + data + ']';
    }
}
