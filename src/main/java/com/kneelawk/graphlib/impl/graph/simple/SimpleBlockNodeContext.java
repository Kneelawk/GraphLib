package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeContext;
import com.kneelawk.graphlib.api.node.PosNodeKey;

public class SimpleBlockNodeContext implements BlockNodeContext {
    private final ServerWorld blockWorld;
    private final SimpleGraphWorld graphWorld;
    private final NodeHolder<BlockNode> self;
    private final PosNodeKey key;

    public SimpleBlockNodeContext(ServerWorld blockWorld, SimpleGraphWorld graphWorld,
                                  NodeHolder<BlockNode> self, PosNodeKey key) {
        this.blockWorld = blockWorld;
        this.graphWorld = graphWorld;
        this.self = self;
        this.key = key;
    }

    @Override
    public void markDirty() {
        graphWorld.markDirty(getGraphId());
    }

    @Override
    public @NotNull ServerWorld getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphWorld getGraphWorld() {
        return graphWorld;
    }

    @Override
    public long getGraphId() {
        return self.getGraphId();
    }

    @Override
    public @Nullable BlockGraph getGraph() {
        return graphWorld.getGraph(getGraphId());
    }

    @Override
    public @Nullable NodeHolder<BlockNode> getSelf() {
        return self;
    }

    @Override
    public @NotNull PosNodeKey getKey() {
        return key;
    }
}
