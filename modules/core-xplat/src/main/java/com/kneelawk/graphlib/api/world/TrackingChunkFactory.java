package com.kneelawk.graphlib.api.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.SectionPos;

/**
 * Creates a storage chunk that can alert its holder when it has changed.
 *
 * @param <R> the type of storage chunk this creates.
 */
@FunctionalInterface
public interface TrackingChunkFactory<R> {
    /**
     * Create a new storage chunk.
     *
     * @param pos       the position of the storage chunk being created.
     * @param markDirty used to signal when the created storage chunk has changed.
     * @return the newly created storage chunk.
     */
    @NotNull
    R createNew(@NotNull SectionPos pos, @NotNull Runnable markDirty);
}
