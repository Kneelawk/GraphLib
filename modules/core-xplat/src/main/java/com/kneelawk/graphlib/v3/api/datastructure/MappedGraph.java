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
 * @param <K> the type of key used to index each node.
 * @param <L> the type of link data this graph contains between nodes.
 */
public final class MappedGraph<K, L> implements Iterable<KeyedNode<K, L>> {
    private final Map<K, KeyedNode<K, L>> nodes = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Constructs an empty graph.
     */
    public MappedGraph() {
    }

    /**
     * Adds a node to this graph containing the given data.
     *
     * @param key the key of the new node.
     * @return the new node.
     */
    public KeyedNode<K, L> add(K key) {
        KeyedNode<K, L> node = new KeyedNode<>(key);
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
    public @Nullable KeyedNode<K, L> get(K key) {
        return nodes.get(key);
    }

    /**
     * Gets the node associated with the given key if one is present or throws and exception.
     *
     * @param key the key of the node to find.
     * @return the found node.
     * @throws NoSuchElementException if no node is present with the given key.
     */
    public KeyedNode<K, L> getOrThrow(K key) {
        KeyedNode<K, L> node = get(key);
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
            KeyedNode<K, L> node = nodes.remove(key);
            nodes.forEach((_k, n) -> n.onRemoved(node));
        }
    }

    /**
     * Splits all disconnected sets of nodes off into their own graphs.
     * <p>
     * Note: this graph will always retain the largest body of nodes, returning the smaller bodies in the list.
     *
     * @return the new graphs made from the disconnected nodes.
     */
    public List<MappedGraph<K, L>> split() {
        List<MappedGraph<K, L>> result = new ArrayList<>();
        int largestGraphSize = 0;
        int largestGraphIndex = 0;

        Map<K, KeyedNode<K, L>> toBeChecked = new Object2ObjectLinkedOpenHashMap<>(nodes);
        Map<K, KeyedNode<K, L>> connected = new Object2ObjectLinkedOpenHashMap<>();

        while (!toBeChecked.isEmpty()) {
            connected.clear();
            descend(connected, toBeChecked, toBeChecked.values().iterator().next());

            if (!toBeChecked.isEmpty()) {
                MappedGraph<K, L> newGraph = new MappedGraph<>();
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
            MappedGraph<K, L> newGraph = new MappedGraph<>();
            moveBulkUnchecked(newGraph, connected);
            MappedGraph<K, L> largestGraph = result.set(largestGraphIndex, newGraph);
            join(largestGraph);
        }

        return result;
    }

    private void descend(Map<K, KeyedNode<K, L>> connected, Map<K, KeyedNode<K, L>> toBeChecked, KeyedNode<K, L> node) {
        Deque<KeyedNode<K, L>> stack = new ArrayDeque<>();
        stack.push(node);

        connected.put(node.key(), node);
        toBeChecked.remove(node.key());

        while (!stack.isEmpty()) {
            KeyedNode<K, L> cur = stack.pop();

            for (KeyedLink<K, L> link : cur.connections()) {
                KeyedNode<K, L> a = link.other(cur);

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
    public void moveBulkUnchecked(MappedGraph<K, L> into, Map<K, KeyedNode<K, L>> nodes) {
        this.nodes.keySet().removeAll(nodes.keySet());
        into.nodes.putAll(nodes);
    }

    /**
     * Joins this graph with another graph, moving all its nodes into this graph.
     *
     * @param other the other graph to join with.
     */
    public void join(MappedGraph<K, L> other) {
        this.nodes.putAll(other.nodes);
        other.nodes.clear();
    }

    /**
     * Links the nodes with the two given keys.
     *
     * @param aKey    the key of the first node to link.
     * @param bKey    the key of the second node to link.
     * @param linkKey the key of the link itself.
     * @return the link object if both nodes are actually present in this graph.
     */
    public @Nullable KeyedLink<K, L> link(K aKey, K bKey, L linkKey) {
        KeyedNode<K, L> a = get(aKey);
        KeyedNode<K, L> b = get(bKey);
        if (a == null || b == null) return null;
        return link(a, b, linkKey);
    }

    /**
     * Links two nodes.
     *
     * @param a       the first node to link.
     * @param b       the second node to link.
     * @param linkKey the key for the new link.
     * @return the link between the two nodes.
     */
    public KeyedLink<K, L> link(KeyedNode<K, L> a, KeyedNode<K, L> b, L linkKey) {
        KeyedLink<K, L> link = new KeyedLink<>(a, b, linkKey);
        a.onLink(link);
        b.onLink(link);
        return link;
    }

    /**
     * Links the two nodes in a link object.
     *
     * @param newLink the link object describing the link to be created.
     * @return <code>true</code> the two nodes were not previously linked already, <code>false</code> otherwise.
     */
    public boolean link(KeyedLink<K, L> newLink) {
        return newLink.first().onLink(newLink) & newLink.second().onLink(newLink);
    }

    /**
     * Unlinks two nodes.
     * <p>
     * Note: this tries unlinking in both directions, so node order is not an issue.
     *
     * @param aKey    the key of the first node to unlink.
     * @param bKey    the key of the second node to unlink.
     * @param linkKey the key of the link itself.
     * @return {@code true} if a link was removed from both nodes, {@code false} otherwise.
     */
    public boolean unlink(K aKey, K bKey, L linkKey) {
        KeyedNode<K, L> a = get(aKey);
        KeyedNode<K, L> b = get(bKey);
        if (a == null || b == null) return false;
        return unlink(a, b, linkKey);
    }

    /**
     * Unlinks two nodes.
     * <p>
     * Note: this tries unlinking in both directions, so node order is not an issue.
     *
     * @param a       the first node to unlink.
     * @param b       the second node to unlink.
     * @param linkKey the key of the link to be removed.
     * @return <code>true</code> if a link was removed from both nodes, <code>false</code> otherwise.
     */
    public boolean unlink(KeyedNode<K, L> a, KeyedNode<K, L> b, L linkKey) {
        KeyedLink<K, L> link1 = new KeyedLink<>(a, b, linkKey);
        return a.onUnlink(link1) & b.onUnlink(link1);
    }

    /**
     * Unlinks the two nodes in a link object.
     * <p>
     * Note: this tries unlinking in both directions, so node order is not an issue.
     *
     * @param link the link object describing the link to be removed.
     * @return <code>true</code> if a link was removed from both nodes, <code>false</code> otherwise.
     */
    public boolean unlink(KeyedLink<K, L> link) {
        return link.first().onUnlink(link) & link.second().onUnlink(link);
    }

    /**
     * Checks to see if this graph contains the given link.
     *
     * @param aKey    the key of the first node in the link.
     * @param bKey    the key of the second node in the link.
     * @param linkKey the key of the link itself.
     * @return {@code true} if the described link is present in this graph.
     */
    public boolean containsLink(K aKey, K bKey, L linkKey) {
        KeyedNode<K, L> a = get(aKey);
        KeyedNode<K, L> b = get(bKey);
        if (a == null || b == null) return false;
        KeyedLink<K, L> link = new KeyedLink<>(a, b, linkKey);
        return containsLink(link);
    }

    /**
     * Checks to see if this graph contains the given link.
     *
     * @param link the link to check.
     * @return whether this graph contains the given link.
     */
    public boolean containsLink(KeyedLink<K, L> link) {
        return link.first().connections().contains(link) && link.second().connections().contains(link);
    }

    @Override
    public Iterator<KeyedNode<K, L>> iterator() {
        return nodes.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super KeyedNode<K, L>> action) {
        nodes.values().forEach(action);
    }

    @Override
    public Spliterator<KeyedNode<K, L>> spliterator() {
        return nodes.values().spliterator();
    }

    /**
     * Returns a stream of all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    public Stream<KeyedNode<K, L>> stream() {
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
