package com.kneelawk.graphlib.graph;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

public final class Node<T> {
    private final T data;
    private final Set<Link<T>> connections;

    public Node(T data) {
        this.data = data;
        this.connections = new LinkedHashSet<>();
    }

    public T data() {
        return data;
    }

    public Set<Link<T>> connections() {
        return connections;
    }

    public void onAdded(Node<T> other) {
    }

    public void onRemoved(Node<T> other) {
        connections.removeIf(link -> link.contains(other));
    }

    public void onLink(Link<T> link) {
        connections.add(link);
    }

    public void onUnlink(Link<T> link) {
        connections.remove(link);
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
