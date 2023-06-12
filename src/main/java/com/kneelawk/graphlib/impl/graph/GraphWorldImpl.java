package com.kneelawk.graphlib.impl.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.ChunkPos;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphWorld;

public interface GraphWorldImpl extends GraphWorld, AutoCloseable {

    void onWorldChunkLoad(@NotNull ChunkPos pos);

    void onWorldChunkUnload(@NotNull ChunkPos pos);

    void tick();

    void saveChunk(@NotNull ChunkPos pos);

    void saveAll(boolean flush);

    /**
     * Called by the <code>/graphlib removeemptygraphs</code> command.
     * <p>
     * Removes all empty graphs. Graphs should never be empty, but it could theoretically happen if the server crashes
     * and some things didn't get saved.
     *
     * @return the number of empty graphs removed.
     */
    int removeEmptyGraphs();

    void writeChunkPillar(ChunkPos pos, NetByteBuf buf, IMsgWriteCtx ctx);
}
