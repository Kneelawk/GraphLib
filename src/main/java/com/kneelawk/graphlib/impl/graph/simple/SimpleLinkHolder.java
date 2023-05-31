package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.graph.Link;
import com.kneelawk.graphlib.api.util.graph.Node;

public class SimpleLinkHolder<K extends LinkKey> implements LinkHolder<K> {
    private final Link<SimpleNodeWrapper, K> link;

    public SimpleLinkHolder(Link<SimpleNodeWrapper, K> link) {
        this.link = link;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeHolder<BlockNode> getFirst() {
        return new SimpleNodeHolder<>((Node<SimpleNodeWrapper, LinkKey>) link.first());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeHolder<BlockNode> getSecond() {
        return new SimpleNodeHolder<>((Node<SimpleNodeWrapper, LinkKey>) link.second());
    }

    @Override
    public @NotNull K getKey() {
        return link.key();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLinkHolder<?> that = (SimpleLinkHolder<?>) o;
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
