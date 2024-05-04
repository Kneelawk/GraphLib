package com.kneelawk.graphlib.impl.graph;

import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;

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
    void rebuildChunks(List<SectionPos> toRebuild, RebuildChunksListener listener);

    @Override
    @NotNull ServerLevel getWorld();

    @Nullable WorldListener getListener(ResourceLocation id);

    @Override
    @Nullable BlockGraphImpl getGraph(long id);
}
