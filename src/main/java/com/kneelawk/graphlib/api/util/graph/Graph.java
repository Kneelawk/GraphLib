package com.kneelawk.graphlib.api.util.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * General purpose graph data structure.
 *
 * @param <T> the type of data this graph contains in each node.
 */
public final class Graph<T> implements Iterable<Node<T>> {
    private final Set<Node<T>> nodes = new LinkedHashSet<>();

    /**
     * Constructs an empty graph.
     */
    public Graph() {
    }

    /**
     * Adds a node to this graph containing the given data.
     *
     * @param data the data for the new node to contain.
     * @return the new node.
     */
    public @NotNull Node<T> add(T data) {
        Node<T> node = new Node<>(data);
        nodes.forEach(n -> n.onAdded(node));
        nodes.add(node);
        return node;
    }

    /**
     * Removes a node from this graph.
     * <p>
     * Note: this does not perform graph splitting. That must be done separately.
     *
     * @param node the node to remove.
     */
    public void remove(@NotNull Node<T> node) {
        if (nodes.contains(node)) {
            nodes.remove(node);
            nodes.forEach(n -> n.onRemoved(node));
        }
    }

    /**
     * Splits all disconnected sets of nodes off into their own graphs.
     * <p>
     * Note: this graph will always retain the largest body of nodes, returning the smaller bodies in the list.
     *
     * @return the new graphs made from the disconnected nodes.
     */
    public @NotNull List<Graph<T>> split() {
        List<Graph<T>> result = new ArrayList<>();
        int largestGraphSize = 0;
        int largestGraphIndex = 0;

        Set<Node<T>> toBeChecked = new LinkedHashSet<>(nodes);
        Set<Node<T>> connected = new LinkedHashSet<>();

        while (!toBeChecked.isEmpty()) {
            connected.clear();
            descend(connected, toBeChecked, toBeChecked.iterator().next());

            if (!toBeChecked.isEmpty()) {
                Graph<T> newGraph = new Graph<>();
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
            Graph<T> newGraph = new Graph<>();
            moveBulkUnchecked(newGraph, connected);
            Graph<T> largestGraph = result.set(largestGraphIndex, newGraph);
            join(largestGraph);
        }

        return result;
    }

    private void descend(@NotNull Set<Node<T>> connected, @NotNull Set<Node<T>> toBeChecked, @NotNull Node<T> node) {
        Deque<Node<T>> stack = new ArrayDeque<>();
        stack.push(node);

        connected.add(node);
        toBeChecked.remove(node);

        while (!stack.isEmpty()) {
            Node<T> cur = stack.pop();

            for (Link<T> link : cur.connections()) {
                Node<T> a = link.other(cur);

                if (toBeChecked.contains(a)) {
                    stack.push(a);
                    connected.add(a);
                    toBeChecked.remove(a);
                }
            }
        }
    }

    private void moveBulkUnchecked(@NotNull Graph<T> into, @NotNull Set<Node<T>> nodes) {
        this.nodes.removeAll(nodes);
        into.nodes.addAll(nodes);
    }

    /**
     * Joins this graph with another graph, moving all its nodes into this graph.
     *
     * @param other the other graph to join with.
     */
    public void join(@NotNull Graph<T> other) {
        this.nodes.addAll(other.nodes);
        other.nodes.clear();
    }

    /**
     * Links two nodes.
     *
     * @param a the first node to link.
     * @param b the second node to link.
     * @return the link between the two nodes.
     */
    public @NotNull Link<T> link(@NotNull Node<T> a, @NotNull Node<T> b) {
        Link<T> link = new Link<>(a, b);
        a.onLink(link);
        b.onLink(link);
        return link;
    }

    /**
     * Unlinks two nodes.
     * <p>
     * Note: this tries unlinking in both directions, so node order is not an issue.
     *
     * @param a the first node to unlink.
     * @param b the second node to unlink.
     */
    public void unlink(@NotNull Node<T> a, @NotNull Node<T> b) {
        Link<T> link1 = new Link<>(a, b);
        Link<T> link2 = new Link<>(b, a);
        a.onUnlink(link1);
        b.onUnlink(link1);
        a.onUnlink(link2);
        b.onUnlink(link2);
    }

    /**
     * Checks to see if this graph contains the given node.
     *
     * @param node the node to check.
     * @return whether this graph contains the given node.
     */
    public boolean contains(@NotNull Node<T> node) {
        return nodes.contains(node);
    }

    /**
     * Checks to see if this graph contains all the given nodes.
     *
     * @param nodes the nodes to check to see if this graph contains.
     * @return whether this graph contains all the given nodes.
     */
    @SafeVarargs
    public final boolean contains(@NotNull Node<T>... nodes) {
        for (Node<T> node : nodes) {
            if (!contains(node))
                return false;
        }
        return true;
    }

    @NotNull
    @Override
    public Iterator<Node<T>> iterator() {
        return nodes.iterator();
    }

    @Override
    public void forEach(@NotNull Consumer<? super Node<T>> action) {
        nodes.forEach(action);
    }

    @NotNull
    @Override
    public Spliterator<Node<T>> spliterator() {
        return nodes.spliterator();
    }

    /**
     * Returns a stream of all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    public @NotNull Stream<Node<T>> stream() {
        return nodes.stream();
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
