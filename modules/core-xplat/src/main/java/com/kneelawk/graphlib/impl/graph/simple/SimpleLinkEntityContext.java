package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import net.minecraft.world.level.Level;

public record SimpleLinkEntityContext(LinkHolder<LinkKey> holder, Level blockWorld, SimpleGraphCollection graphWorld)
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
    public @NotNull Level getBlockWorld() {
        return blockWorld;
    }

    @Override
    public @NotNull GraphView getGraphWorld() {
        return graphWorld;
    }
}
