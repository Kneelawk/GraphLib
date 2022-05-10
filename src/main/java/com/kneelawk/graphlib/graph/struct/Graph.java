package com.kneelawk.graphlib.graph.struct;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * General purpose graph data structure.
 *
 * @param <T> the type of data this graph contains in each node.
 */
public final class Graph<T> implements Iterable<Node<T>> {
    private final Set<Node<T>> nodes = new LinkedHashSet<>();

    public @NotNull Node<T> add(T data) {
        Node<T> node = new Node<>(data);
        nodes.forEach(n -> n.onAdded(node));
        nodes.add(node);
        return node;
    }

    public void remove(@NotNull Node<T> node) {
        if (nodes.contains(node)) {
            nodes.remove(node);
            nodes.forEach(n -> n.onRemoved(node));
        }
    }

    public @NotNull List<Graph<T>> split() {
        List<Graph<T>> result = new ArrayList<>();
        Set<Node<T>> toBeChecked = new LinkedHashSet<>(nodes);

        while (!toBeChecked.isEmpty()) {
            Set<Node<T>> connected = new LinkedHashSet<>();
            descend(connected, toBeChecked, toBeChecked.iterator().next());

            if (!toBeChecked.isEmpty()) {
                Graph<T> newGraph = new Graph<>();
                moveBulkUnchecked(newGraph, connected);
                result.add(newGraph);
            }
        }

        return result;
    }

    private void descend(@NotNull Set<Node<T>> connected, @NotNull Set<Node<T>> toBeChecked, @NotNull Node<T> node) {
        connected.add(node);
        toBeChecked.remove(node);

        for (Link<T> link : node.connections()) {
            Node<T> a = link.other(node);

            if (toBeChecked.contains(a)) {
                descend(connected, toBeChecked, a);
            }
        }
    }

    private void moveBulkUnchecked(@NotNull Graph<T> into, @NotNull Set<Node<T>> nodes) {
        this.nodes.removeAll(nodes);
        into.nodes.addAll(nodes);
    }

    public void join(@NotNull Graph<T> other) {
        this.nodes.addAll(other.nodes);
        other.nodes.clear();
    }

    public @NotNull Link<T> link(@NotNull Node<T> a, @NotNull Node<T> b) {
        Link<T> link = new Link<>(a, b);
        a.onLink(link);
        b.onLink(link);
        return link;
    }

    public void unlink(@NotNull Node<T> a, @NotNull Node<T> b) {
        Link<T> link1 = new Link<>(a, b);
        Link<T> link2 = new Link<>(b, a);
        a.onUnlink(link1);
        b.onUnlink(link1);
        a.onUnlink(link2);
        b.onUnlink(link2);
    }

    public boolean contains(@NotNull Node<T> node) {
        return nodes.contains(node);
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

    public @NotNull Stream<Node<T>> stream() {
        return nodes.stream();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int size() {
        return nodes.size();
    }
}
