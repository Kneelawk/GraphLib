package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.SnapshotNode;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.util.ReadOnlyMappingMap;

public class SimpleNodeHolder<T extends BlockNode> implements NodeHolder<T> {
    final Node<PosNodeKey, SimpleNodeWrapper> node;

    /**
     * @param node treat this as if it were parameterized on <code>&lt;T&gt;</code>.
     */
    public SimpleNodeHolder(Node<PosNodeKey, SimpleNodeWrapper> node) {
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
    public @NotNull Map<PosNodeKey, NodeLink> getConnections() {
        return new ReadOnlyMappingMap<>(node.connections(), SimpleNodeLink::new, conn -> {
            if (conn instanceof SimpleNodeLink simple) {
                return simple.getLink();
            } else {
                return null;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull SnapshotNode<T> toSnapshot() {
        return new SnapshotNode<>(node.key().pos(), (T) node.value().getNode(), node.value().getGraphId());
    }

    @Override
    public @NotNull PosNodeKey getNodeKey() {
        return node.key();
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
