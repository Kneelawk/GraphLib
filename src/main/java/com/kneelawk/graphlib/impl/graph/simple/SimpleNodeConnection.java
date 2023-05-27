package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeConnection;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.graph.Link;

public class SimpleNodeConnection implements NodeConnection {
    private final Link<SimpleNodeWrapper> link;

    public SimpleNodeConnection(Link<SimpleNodeWrapper> link) {
        this.link = link;
    }

    @Override
    public @NotNull NodeHolder<BlockNode> getFirst() {
        return new SimpleNodeHolder<>(link.first());
    }

    @Override
    public @NotNull NodeHolder<BlockNode> getSecond() {
        return new SimpleNodeHolder<>(link.second());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleNodeConnection that = (SimpleNodeConnection) o;
        return Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
