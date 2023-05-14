package com.kneelawk.graphlib.api.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

/**
 * Describes something that can store chunk-sections worth of data.
 *
 * @param <R> the type of chunk-section data to be stored.
 */
public interface RegionBasedStorage<R extends StorageChunk> extends AutoCloseable {
    /**
     * Indicates to this that a world chunk is being loaded and that the corresponding chunk pillar should be loaded
     * here as well.
     *
     * @param pos the position of the world chunk that is being loaded.
     */
    default void onWorldChunkLoad(@NotNull ChunkPos pos) {
    }

    /**
     * Indicates to this that a world chunk is being unloaded and that the corresponding chunk pillar should be
     * scheduled for unloading here as well.
     *
     * @param pos the position of the world chunk that is being unloaded.
     */
    default void onWorldChunkUnload(@NotNull ChunkPos pos) {
    }

    /**
     * Gets a chunk section at the given location or creates one if none existed there already.
     *
     * @param pos the position of the chunk section.
     * @return the retrieved or created chunk section.
     */
    @NotNull R getOrCreate(@NotNull ChunkSectionPos pos);

    /**
     * Gets a chunk section at the given location or <code>null</code> one does not exist there.
     *
     * @param pos the position of the chunk section.
     * @return the retrieved chunk section, or <code>null</code> if none could be retrieved.
     */
    @Nullable R getIfExists(@NotNull ChunkSectionPos pos);

    /**
     * Ticks this storage, unloading and saving any chunks that need it.
     */
    void tick();

    /**
     * Forcefully saves all chunks, but leaves them loaded.
     */
    void saveAll();

    /**
     * Forcefully saves a specific chunk, but leaves it loaded.
     *
     * @param pos the position of the chunk to save.
     */
    void saveChunk(@NotNull ChunkPos pos);
}
