package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.util.graph.Link;

public class SimpleNodeLink implements NodeLink {
    private final Link<PosNodeKey, SimpleNodeWrapper> link;

    public SimpleNodeLink(Link<PosNodeKey, SimpleNodeWrapper> link) {
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

    Link<PosNodeKey, SimpleNodeWrapper> getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleNodeLink that = (SimpleNodeLink) o;
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
