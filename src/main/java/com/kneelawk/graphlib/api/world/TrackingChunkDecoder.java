package com.kneelawk.graphlib.api.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkSectionPos;

/**
 * Decodes a storage chunk that can alert its holder when it has changed.
 *
 * @param <R> the type of storage chunk being decoded.
 */
public interface TrackingChunkDecoder<R extends StorageChunk> {
    /**
     * Decode a storage chunk.
     *
     * @param compound  the NBT data to decode from.
     * @param pos       the position of the storage chunk being decoded.
     * @param markDirty used to signal when the decoded chunk has changed.
     * @return a newly decoded storage chunk.
     */
    @NotNull R decode(@NotNull NbtCompound compound, @NotNull ChunkSectionPos pos, @NotNull Runnable markDirty);
}
