package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;

public record SimpleNodeEntityContext(@NotNull NodeHolder<BlockNode> holder, @NotNull World blockWorld,
                                      @NotNull SimpleGraphCollection graphWorld) implements NodeEntityContext {
    @Override
    public void markDirty() {
        graphWorld.markDirty(getGraphId());
    }

    @Override
    public @NotNull NodeHolder<BlockNode> getHolder() {
        return holder;
    }

    @Override
    public @NotNull World getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphView getGraphWorld() {
        return graphWorld;
    }
}
