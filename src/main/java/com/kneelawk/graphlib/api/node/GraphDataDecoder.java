package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;

/**
 * A decoder for graph data.
 *
 * @param <G> the type of graph data this decoder decodes.
 */
@FunctionalInterface
public interface GraphDataDecoder<G extends GraphData<G>> {
    /**
     * Decodes graph data from the given NBT tag.
     *
     * @param tag        the NBT tag to decode graph data from.
     * @param markDirty  call this when graph data has changed, to let GraphLib know it needs to save the graph.
     * @param blockWorld the block world that this data's graph is in.
     * @param graphWorld the graph world that this data's graph is in.
     * @param graph      the block graph that this data will be a part of.
     * @return a newly decoded graph data, or <code>null</code> if an error occurred while decoding.
     */
    @Nullable G decode(@Nullable NbtElement tag, @NotNull Runnable markDirty, @NotNull ServerWorld blockWorld,
                       @NotNull GraphWorld graphWorld, @NotNull BlockGraph graph);
}
