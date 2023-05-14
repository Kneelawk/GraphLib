package com.kneelawk.graphlib.api.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.ChunkSectionPos;

/**
 * Creates a storage chunk that can alert its holder when it has changed.
 *
 * @param <R> the type of storage chunk this creates.
 */
public interface TrackingChunkFactory<R extends StorageChunk> {
    /**
     * Create a new storage chunk.
     *
     * @param pos       the position of the storage chunk being created.
     * @param markDirty used to signal when the created storage chunk has changed.
     * @return the newly created storage chunk.
     */
    @NotNull R createNew(@NotNull ChunkSectionPos pos, @NotNull Runnable markDirty);
}
