package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.NodeConnection;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.SnapshotNode;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.util.ReadOnlyMappingCollection;

public class SimpleNodeHolder<T extends BlockNode> implements NodeHolder<T> {
    final Node<SimpleNodeWrapper> node;

    /**
     * @param node treat this as if it were parameterized on <code>&lt;T&gt;</code>.
     */
    public SimpleNodeHolder(Node<SimpleNodeWrapper> node) {
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
    public @NotNull Collection<NodeConnection> getConnections() {
        return new ReadOnlyMappingCollection<>(node.connections(), SimpleNodeConnection::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull SnapshotNode<T> toSnapshot() {
        return new SnapshotNode<>(node.data().getPos(), (T) node.data().getNode(), node.data().getGraphId());
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
