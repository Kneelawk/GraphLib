package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.node.BlockNode;

public final class SimpleNodeWrapper {
    final @NotNull BlockNode node;

    long graphId;

    public SimpleNodeWrapper(@NotNull BlockNode node, long graphId) {
        this.node = node;
        this.graphId = graphId;
    }

    public @NotNull BlockNode getNode() {
        return node;
    }

    public long getGraphId() {
        return graphId;
    }
}
