package com.kneelawk.graphlib.impl.graph;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

public interface ServerGraphWorldImpl extends GraphWorld, AutoCloseable {

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

    /**
     * Starts a chunk rebuilding task.
     *
     * @param toRebuild the chunks to rebuild.
     * @param listener  progress and completion listeners.
     */
    void rebuildChunks(List<ChunkSectionPos> toRebuild, RebuildChunksListener listener);

    @Override
    @NotNull ServerWorld getWorld();

    void writeChunkPillar(ChunkPos pos, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeMerge(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeLink(BlockGraph graph, LinkHolder<LinkKey> link, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeUnlink(BlockGraph graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key, NetByteBuf buf,
                     IMsgWriteCtx ctx);

    void writeSplitInto(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeNodeRemove(BlockGraph graph, NodeHolder<BlockNode> holder, NetByteBuf buf, IMsgWriteCtx ctx);
}
