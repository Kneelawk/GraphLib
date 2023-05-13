package com.kneelawk.graphlib.api.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;

/**
 * Represents a chunk section of storage, stored in a {@link UnloadingRegionBasedStorage}.
 */
public interface StorageChunk {
    /**
     * Writes this chunk section to NBT.
     *
     * @param nbt the NBT Compound to write this to.
     */
    void toNbt(@NotNull NbtCompound nbt);
}
