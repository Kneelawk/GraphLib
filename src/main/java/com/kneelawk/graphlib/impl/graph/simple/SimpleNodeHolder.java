package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.graph.SnapshotNode;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.graph.Link;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.util.ReadOnlyMappingCollection;

public class SimpleNodeHolder<T extends BlockNode> implements NodeHolder<T> {
    final Node<SimpleNodeWrapper, LinkKey> node;

    /**
     * @param node treat this as if it were parameterized on <code>&lt;T&gt;</code>.
     */
    public SimpleNodeHolder(Node<SimpleNodeWrapper, LinkKey> node) {
        this.node = node;
    }

    @Override
    public @NotNull BlockPos getPos() {
        return node.data().getPos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull T getNode() {
        return (T) node.data().getNode();
    }

    @Override
    public long getGraphId() {
        return node.data().getGraphId();
    }

    @Override
    public @NotNull Collection<NodeLink<LinkKey>> getConnections() {
        return new ReadOnlyMappingCollection<>(node.connections(), SimpleNodeLink::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <K extends LinkKey> Stream<NodeLink<K>> getConnectionsOfType(Class<K> keyClass) {
        return node.connections().stream().filter(link -> keyClass.isInstance(link.key()))
            .map(link -> new SimpleNodeLink<>((Link<SimpleNodeWrapper, K>) link));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <K extends LinkKey> Stream<NodeLink<K>> getConnectionsThatMatch(Class<K> keyClass,
                                                                                    Predicate<K> filter) {
        return node.connections().stream()
            .filter(link -> keyClass.isInstance(link.key()) && filter.test(keyClass.cast(link.key())))
            .map(link -> new SimpleNodeLink<>((Link<SimpleNodeWrapper, K>) link));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull SnapshotNode<T> toSnapshot() {
        return new SnapshotNode<>(node.data().getPos(), (T) node.data().getNode(), node.data().getGraphId());
    }

    @Override
    public @NotNull NodePos toNodePos() {
        return new NodePos(node.data().getPos(), node.data().getNode());
    }

    @Override
    public boolean canCast(Class<?> newType) {
        return newType.isInstance(node.data().getNode());
    }

    @Override
    public <R extends BlockNode> NodeHolder<R> cast(Class<R> newType) throws ClassCastException {
        if (!canCast(newType))
            throw new ClassCastException(node.data().getNode().getClass() + " cannot be cast to " + newType);
        return new SimpleNodeHolder<>(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleNodeHolder<?> that = (SimpleNodeHolder<?>) o;
        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}
