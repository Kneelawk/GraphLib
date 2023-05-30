package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

public record SimpleLinkEntityContext(LinkHolder<LinkKey> holder, ServerWorld blockWorld, SimpleGraphWorld graphWorld)
    implements LinkEntityContext {
    @Override
    public void markDirty() {
        graphWorld.markDirty(holder.getFirst().getGraphId());
    }

    @Override
    public @NotNull LinkHolder<LinkKey> getHolder() {
        return holder;
    }

    @Override
    public @NotNull ServerWorld getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphView getGraphWorld() {
        return graphWorld;
    }
}
