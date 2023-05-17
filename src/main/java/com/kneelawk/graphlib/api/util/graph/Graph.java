package com.kneelawk.graphlib.api.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.errorprone.annotations.CompatibleWith;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * General purpose graph data structure.
 *
 * @param <K> the type of node keys used.
 * @param <V> the type of node value contained.
 */
public final class Graph<K, V> implements Iterable<Node<K, V>> {
    private final Object2ObjectMap<K, Node<K, V>> nodes = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Constructs an empty graph.
     */
    public Graph() {
    }

    /**
     * Adds a node to this graph containing the given data.
     *
     * @param key   the new node's unique key.
     * @param value the value for the new node to contain.
     * @return the new node.
     */
    public @NotNull Node<K, V> add(K key, V value) {
        Node<K, V> node = new Node<>(key, value);
        nodes.values().forEach(n -> n.nodeAdded(node));
        nodes.put(key, node);
        return node;
    }

    /**
     * Removes a node from this graph.
     * <p>
     * Note: this does not perform graph splitting. That must be done separately.
     *
     * @param key the key of the node to remove.
     */
    public void remove(@NotNull K key) {
        if (nodes.containsKey(key)) {
            nodes.remove(key);
            nodes.values().forEach(n -> n.nodeRemoved(key));
        }
    }

    /**
     * Gets the node in this graph with the given key.
     *
     * @param key the key of the node to get.
     * @return the node with the given key.
     */
    public @Nullable Node<K, V> get(@NotNull K key) {
        return nodes.get(key);
    }

    /**
     * Splits all disconnected sets of nodes off into their own graphs.
     * <p>
     * Note: this graph will always retain the largest body of nodes, returning the smaller bodies in the list.
     *
     * @return the new graphs made from the disconnected nodes.
     */
    public @NotNull List<Graph<K, V>> split() {
        List<Graph<K, V>> result = new ArrayList<>();
        int largestGraphSize = 0;
        int largestGraphIndex = 0;

        SortedMap<K, Node<K, V>> toBeChecked = new Object2ObjectLinkedOpenHashMap<>(nodes);
        Map<K, Node<K, V>> connected = new Object2ObjectLinkedOpenHashMap<>();

        while (!toBeChecked.isEmpty()) {
            connected.clear();
            descend(connected, toBeChecked, toBeChecked.firstKey());

            if (!toBeChecked.isEmpty()) {
                Graph<K, V> newGraph = new Graph<>();
                moveBulkUnchecked(newGraph, connected);

                if (newGraph.size() > largestGraphSize) {
                    largestGraphSize = newGraph.size();
                    largestGraphIndex = result.size();
                }

                result.add(newGraph);
            }
        }

        if (connected.size() < largestGraphSize) {
            // find the largest graph and make it ours
            Graph<K, V> newGraph = new Graph<>();
            moveBulkUnchecked(newGraph, connected);
            Graph<K, V> largestGraph = result.set(largestGraphIndex, newGraph);
            join(largestGraph);
        }

        return result;
    }

    private void descend(@NotNull Map<K, Node<K, V>> connected, @NotNull Map<K, Node<K, V>> toBeChecked,
                         @NotNull K key) {
        Node<K, V> firstNode = toBeChecked.get(key);
        Deque<K> stack = new ArrayDeque<>();
        stack.push(key);

        connected.put(key, firstNode);
        toBeChecked.remove(key);

        while (!stack.isEmpty()) {
            K cur = stack.pop();

            for (Link<K, V> link : connected.get(cur).connections().values()) {
                Node<K, V> a = link.other(cur);

                if (toBeChecked.containsKey(a.key())) {
                    stack.push(a.key());
                    connected.put(a.key(), a);
                    toBeChecked.remove(a.key());
                }
            }
        }
    }

    private void moveBulkUnchecked(@NotNull Graph<K, V> into, @NotNull Map<K, Node<K, V>> nodes) {
        for (K key : nodes.keySet()) {
            this.nodes.remove(key);
        }
        into.nodes.putAll(nodes);
    }

    /**
     * Joins this graph with another graph, moving all its nodes into this graph.
     *
     * @param other the other graph to join with.
     */
    public void join(@NotNull Graph<K, V> other) {
        this.nodes.putAll(other.nodes);
        other.nodes.clear();
    }

    /**
     * Links two nodes.
     *
     * @param aKey the key of the first node to link.
     * @param bKey the key of the second node to link.
     * @return the link between the two nodes.
     */
    public @Nullable Link<K, V> link(@NotNull K aKey, @NotNull K bKey) {
        Node<K, V> a = nodes.get(aKey);
        if (a == null) return null;

        Node<K, V> b = nodes.get(bKey);
        if (b == null) return null;

        Link<K, V> link = new Link<>(a, b);
        a.addLink(link);
        b.addLink(link);
        return link;
    }

    /**
     * Unlinks two nodes.
     * <p>
     * Note: links are stored by opposite-node, so node order is not an issue.
     *
     * @param aKey the key of the first node to unlink.
     * @param bKey the key of the second node to unlink.
     */
    public @Nullable Link<K, V> unlink(@NotNull K aKey, @NotNull K bKey) {
        Node<K, V> a = nodes.get(aKey);
        if (a == null) return null;

        Node<K, V> b = nodes.get(bKey);
        if (b == null) return null;

        Link<K, V> aLink = a.removeLink(bKey);
        Link<K, V> bLink = b.removeLink(aKey);

        assert aLink == bLink;
        return aLink;
    }

    /**
     * Checks to see if this graph contains the given node.
     *
     * @param nodeKey the key to check.
     * @return whether this graph contains the given node.
     */
    public boolean containsKey(@NotNull @CompatibleWith("K") Object nodeKey) {
        return nodes.containsKey(nodeKey);
    }

    /**
     * Checks to see if this graph contains all the given nodes.
     *
     * @param keys the keys to check to see if this graph contains.
     * @return whether this graph contains all the given nodes.
     */
    public boolean containsKeys(@NotNull @CompatibleWith("K") Object... keys) {
        for (Object node : keys) {
            if (!containsKey(node)) return false;
        }
        return true;
    }

    @NotNull
    @Override
    public Iterator<Node<K, V>> iterator() {
        return nodes.values().iterator();
    }

    @Override
    public void forEach(@NotNull Consumer<? super Node<K, V>> action) {
        nodes.values().forEach(action);
    }

    @NotNull
    @Override
    public Spliterator<Node<K, V>> spliterator() {
        return nodes.values().spliterator();
    }

    /**
     * Returns a stream of all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    public @NotNull Stream<Node<K, V>> stream() {
        return nodes.values().stream();
    }

    /**
     * Returns <code>true</code> if this graph is empty.
     *
     * @return <code>true</code> if this graph is empty.
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Returns the number of nodes in this graph.
     *
     * @return the number of nodes in this graph.
     */
    public int size() {
        return nodes.size();
    }
}
