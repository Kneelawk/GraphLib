package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.graph.Link;

public class SimpleNodeLink implements NodeLink {
    private final Link<SimpleNodeWrapper, LinkKey> link;

    public SimpleNodeLink(Link<SimpleNodeWrapper, LinkKey> link) {
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
    public @NotNull LinkKey getKey() {
        return link.key();
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
