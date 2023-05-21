package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.node.BlockNode;

public final class SimpleNodeWrapper {
    @Nullable BlockNode node = null;

    long graphId;

    public SimpleNodeWrapper(long graphId) {
        this.graphId = graphId;
    }

    public @NotNull BlockNode getNode() {
        if (node == null) throw new IllegalStateException("Node accessed before it has been initialized");
        return node;
    }

    public long getGraphId() {
        return graphId;
    }
}
