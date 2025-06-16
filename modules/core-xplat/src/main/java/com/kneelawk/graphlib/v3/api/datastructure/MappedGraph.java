package com.kneelawk.graphlib.v3.api.datastructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * General purpose graph data structure.
 *
 * @param <K>  the type of key used to index each node.
 * @param <V>  the type of data held within each node.
 * @param <LK> the type of link data this graph contains between nodes.
 * @param <LV> the type of data held within each link.
 */
public final class MappedGraph<K, V, LK, LV> implements Iterable<KeyedNode<K, V, LK, LV>> {
    private final Map<K, KeyedNode<K, V, LK, LV>> nodes = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Constructs an empty graph.
     */
    public MappedGraph() {
    }

    /**
     * Adds a node to this graph containing the given data.
     *
     * @param key   the key of the new node.
     * @param value the value contained within the new node.
     * @return the new node.
     */
    public KeyedNode<K, V, LK, LV> add(K key, V value) {
        KeyedNode<K, V, LK, LV> node = new KeyedNode<>(key, value);
        nodes.forEach((_k, n) -> n.onAdded(node));
        nodes.put(key, node);
        return node;
    }

    /**
     * Gets the node associated with the given key if one is present.
     *
     * @param key the key of the node to find.
     * @return the found node or {@code null} if none was found.
     */
    public @Nullable KeyedNode<K, V, LK, LV> get(K key) {
        return nodes.get(key);
    }

    /**
     * Gets the node associated with the given key if one is present or throws and exception.
     *
     * @param key the key of the node to find.
     * @return the found node.
     * @throws NoSuchElementException if no node is present with the given key.
     */
    public KeyedNode<K, V, LK, LV> getOrThrow(K key) {
        KeyedNode<K, V, LK, LV> node = get(key);
        if (node == null) throw new NoSuchElementException("No node present with key: " + key);
        return node;
    }

    /**
     * Checks to see if this graph contains the given node.
     *
     * @param key the key of the node to check for.
     * @return whether this graph contains the given node.
     */
    public boolean contains(K key) {
        return nodes.containsKey(key);
    }

    /**
     * Checks to see if this graph contains all the given nodes.
     *
     * @param keys the keys of the nodes to check to see if this graph contains.
     * @return whether this graph contains all the given nodes.
     */
    @SafeVarargs
    public final boolean contains(K... keys) {
        for (K key : keys) {
            if (!contains(key))
                return false;
        }
        return true;
    }

    /**
     * Removes a node from this graph.
     * <p>
     * Note: this does not perform graph splitting. That must be done separately.
     *
     * @param key the key of the node to remove.
     */
    public void remove(K key) {
        if (nodes.containsKey(key)) {
            nodes.remove(key);
            nodes.forEach((_k, n) -> n.onRemoved(key));
        }
    }

    /**
     * Splits all disconnected sets of nodes off into their own graphs.
     * <p>
     * Note: this graph will always retain the largest body of nodes, returning the smaller bodies in the list.
     *
     * @return the new graphs made from the disconnected nodes.
     */
    public List<MappedGraph<K, V, LK, LV>> split() {
        List<MappedGraph<K, V, LK, LV>> result = new ArrayList<>();
        int largestGraphSize = 0;
        int largestGraphIndex = 0;

        Map<K, KeyedNode<K, V, LK, LV>> toBeChecked = new Object2ObjectLinkedOpenHashMap<>(nodes);
        Map<K, KeyedNode<K, V, LK, LV>> connected = new Object2ObjectLinkedOpenHashMap<>();

        while (!toBeChecked.isEmpty()) {
            connected.clear();
            descend(connected, toBeChecked, toBeChecked.values().iterator().next());

            if (!toBeChecked.isEmpty()) {
                MappedGraph<K, V, LK, LV> newGraph = new MappedGraph<>();
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
            MappedGraph<K, V, LK, LV> newGraph = new MappedGraph<>();
            moveBulkUnchecked(newGraph, connected);
            MappedGraph<K, V, LK, LV> largestGraph = result.set(largestGraphIndex, newGraph);
            join(largestGraph);
        }

        return result;
    }

    private void descend(Map<K, KeyedNode<K, V, LK, LV>> connected, Map<K, KeyedNode<K, V, LK, LV>> toBeChecked,
                         KeyedNode<K, V, LK, LV> node) {
        Deque<KeyedNode<K, V, LK, LV>> stack = new ArrayDeque<>();
        stack.push(node);

        connected.put(node.key(), node);
        toBeChecked.remove(node.key());

        while (!stack.isEmpty()) {
            KeyedNode<K, V, LK, LV> cur = stack.pop();

            for (KeyedLink<K, V, LK, LV> link : cur.connections().values()) {
                KeyedNode<K, V, LK, LV> a = link.other(cur);

                if (toBeChecked.containsKey(a.key())) {
                    stack.push(a);
                    connected.put(a.key(), a);
                    toBeChecked.remove(a.key());
                }
            }
        }
    }

    /**
     * Moves nodes from this graph into the given graph.
     * <p>
     * <b>WARNING: This does not check node connections. Misuse can result in nodes being connected to other nodes that
     * are not in the same graph. This also does not check if the nodes already existed in the origin or destination
     * graphs.</b>
     *
     * @param into  the graph nodes are being moved into.
     * @param nodes the nodes to be moved.
     */
    public void moveBulkUnchecked(MappedGraph<K, V, LK, LV> into, Map<K, KeyedNode<K, V, LK, LV>> nodes) {
        this.nodes.keySet().removeAll(nodes.keySet());
        into.nodes.putAll(nodes);
    }

    /**
     * Joins this graph with another graph, moving all its nodes into this graph.
     *
     * @param other the other graph to join with.
     */
    public void join(MappedGraph<K, V, LK, LV> other) {
        this.nodes.putAll(other.nodes);
        other.nodes.clear();
    }

    /**
     * Gets a link by its link key, if it exists within this graph.
     *
     * @param key the key of the link to find.
     * @return the found link.
     */
    public @Nullable KeyedLink<K, V, LK, LV> getLink(KeyedLink.Key<K, LK> key) {
        var a = get(key.first());
        var b = get(key.second());
        if (a == null || b == null) return null;
        return a.connections().get(key);
    }

    /**
     * Links the nodes with the two given keys.
     *
     * @param aKey      the key of the first node to link.
     * @param bKey      the key of the second node to link.
     * @param linkKey   the key of the link itself.
     * @param linkValue the value stored within the link.
     * @return the link object if both nodes are actually present in this graph.
     */
    public @Nullable KeyedLink<K, V, LK, LV> link(K aKey, K bKey, LK linkKey, LV linkValue) {
        return link(new KeyedLink.Key<>(aKey, bKey, linkKey), linkValue);
    }

    /**
     * Links the two nodes referenced by the link key.
     *
     * @param key       the link key to create the link for.
     * @param linkValue the value stored within the link.
     * @return the newly created link if none was present with the given key previously.
     */
    public @Nullable KeyedLink<K, V, LK, LV> link(KeyedLink.Key<K, LK> key, LV linkValue) {
        var a = get(key.first());
        var b = get(key.second());
        if (a == null || b == null) return null;
        KeyedLink<K, V, LK, LV> link = new KeyedLink<>(a, b, key, linkValue);
        if (link.link()) return link;
        return null;
    }

    /**
     * Unlinks two nodes.
     * <p>
     * Note: this tries unlinking in both directions, so node order is not an issue.
     *
     * @param aKey    the key of the first node to unlink.
     * @param bKey    the key of the second node to unlink.
     * @param linkKey the key of the link itself.
     * @return the link that was just unlinked, if present.
     */
    public @Nullable KeyedLink<K, V, LK, LV> unlink(K aKey, K bKey, LK linkKey) {
        return unlink(new KeyedLink.Key<>(aKey, bKey, linkKey));
    }

    /**
     * Unlinks the link with the given key.
     *
     * @param key the key of the link to unlink.
     * @return the link that was just unlinked, if present.
     */
    public @Nullable KeyedLink<K, V, LK, LV> unlink(KeyedLink.Key<K, LK> key) {
        var link = getLink(key);
        if (link == null) return null;
        link.unlink();
        return link;
    }

    /**
     * Checks to see if this graph contains the given link.
     *
     * @param aKey    the key of the first node in the link.
     * @param bKey    the key of the second node in the link.
     * @param linkKey the key of the link itself.
     * @return {@code true} if the described link is present in this graph.
     */
    public boolean containsLink(K aKey, K bKey, LK linkKey) {
        return containsLink(new KeyedLink.Key<>(aKey, bKey, linkKey));
    }

    /**
     * Checks to see if this graph contains the given link.
     *
     * @param link the link to check.
     * @return whether this graph contains the given link.
     */
    public boolean containsLink(KeyedLink.Key<K, LK> link) {
        var a = get(link.first());
        var b = get(link.second());
        if (a == null || b == null) return false;
        return a.connections().containsKey(link) && b.connections().containsKey(link);
    }

    @Override
    public Iterator<KeyedNode<K, V, LK, LV>> iterator() {
        return nodes.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super KeyedNode<K, V, LK, LV>> action) {
        nodes.values().forEach(action);
    }

    @Override
    public Spliterator<KeyedNode<K, V, LK, LV>> spliterator() {
        return nodes.values().spliterator();
    }

    /**
     * Returns a stream of all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    public Stream<KeyedNode<K, V, LK, LV>> stream() {
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
