package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeContext;

public class SimpleBlockNodeContext implements BlockNodeContext {
    private final long initialGraphId;
    private final ServerWorld blockWorld;
    private final SimpleGraphWorld graphWorld;
    private @Nullable NodeHolder<BlockNode> self = null;
    private final BlockPos pos;

    public SimpleBlockNodeContext(long initialGraphId, ServerWorld blockWorld, SimpleGraphWorld graphWorld,
                                  BlockPos pos) {
        this.initialGraphId = initialGraphId;
        this.blockWorld = blockWorld;
        this.graphWorld = graphWorld;
        this.pos = pos;
    }

    @Override
    public void markDirty() {
        graphWorld.markDirty(getGraphId());
    }

    @Override
    public ServerWorld getBlockWorld() {
        return blockWorld;
    }

    @Override
    public GraphWorld getGraphWorld() {
        return graphWorld;
    }

    @Override
    public long getGraphId() {
        return self == null ? initialGraphId : self.getGraphId();
    }

    @Override
    public @Nullable BlockGraph getGraph() {
        return graphWorld.getGraph(getGraphId());
    }

    @Override
    public @Nullable NodeHolder<BlockNode> getSelf() {
        return self;
    }

    public void setSelf(@NotNull NodeHolder<BlockNode> self) {
        this.self = self;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }
}
