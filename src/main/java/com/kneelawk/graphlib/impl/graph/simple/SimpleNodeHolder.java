package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.NodeConnection;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.NodeKey;
import com.kneelawk.graphlib.api.graph.PositionedNode;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.util.ReadOnlyMappingCollection;

public class SimpleNodeHolder<T extends BlockNode> implements NodeHolder<T> {
    final Node<NodeKey, SimpleNodeWrapper> node;

    /**
     * @param node treat this as if it were parameterized on <code>&lt;T&gt;</code>.
     */
    public SimpleNodeHolder(Node<NodeKey, SimpleNodeWrapper> node) {
        this.node = node;
    }

    @Override
    public @NotNull BlockPos getPos() {
        return node.key().pos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull T getNode() {
        return (T) node.value().getNode();
    }

    @Override
    public long getGraphId() {
        return node.value().getGraphId();
    }

    @Override
    public @NotNull Collection<NodeConnection> getConnections() {
        return new ReadOnlyMappingCollection<>(node.connections(), SimpleNodeConnection::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull PositionedNode<T> toPositionedNode() {
        return new PositionedNode<>(node.key().pos(), (T) node.value().getNode(), node.value().getGraphId());
    }

    @Override
    public boolean canCast(Class<?> newType) {
        return newType.isInstance(node.value().getNode());
    }

    @Override
    public <R extends BlockNode> NodeHolder<R> cast(Class<R> newType) throws ClassCastException {
        if (!canCast(newType))
            throw new ClassCastException(node.value().getNode().getClass() + " cannot be cast to " + newType);
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
