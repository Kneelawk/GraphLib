package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.util.math.ChunkPos;

/**
 * An abstract tickable timer that alerts you when a chunk should unload.
 */
public abstract class ChunkUnloadTimer {
    /**
     * The configured maximum age for chunks or chunk sections.
     */
    protected final long maxAge;
    /**
     * The current time.
     */
    protected long tickAge;

    /**
     * Keeps track of which world chunk-pillars are currently loaded.
     */
    protected final LongSet worldLoadedChunks = new LongOpenHashSet();

    /**
     * Constructs a chunk-unload-timer.
     *
     * @param maxAge the maximum age that a chunk or chunk section can be before it is unloaded.
     */
    public ChunkUnloadTimer(long maxAge) {
        this.maxAge = maxAge;
        tickAge = 0;
    }

    /**
     * Call to indicate that a world chunk-pillar has been re-loaded and that all countdowns for this chunk should be
     * cancelled.
     *
     * @param pos the chunk position of the chunk pillar being loaded.
     */
    public void onWorldChunkLoad(@NotNull ChunkPos pos) {
        worldLoadedChunks.add(pos.toLong());
        removeUnloadMark(pos);
    }

    /**
     * Called by {@link #onWorldChunkLoad(ChunkPos)} to indicate to the implementation to cancel the unload countdown.
     *
     * @param pos the chunk position of the chunk pillar being loaded.
     */
    protected abstract void removeUnloadMark(@NotNull ChunkPos pos);

    /**
     * Call to indicate that a world chunk-pillar is unloading and that this timer should start the countdown for this
     * chunk.
     *
     * @param pos the chunk position of the chunk pillar being unloaded.
     */
    public void onWorldChunkUnload(@NotNull ChunkPos pos) {
        worldLoadedChunks.remove(pos.toLong());
        markForUnloading(pos);
    }

    /**
     * Called by {@link #onWorldChunkUnload(ChunkPos)} to indicate to the implementation to start the unload countdown.
     *
     * @param pos the chunk position of the chunk pillar being unloaded.
     */
    protected abstract void markForUnloading(@NotNull ChunkPos pos);

    /**
     * Can be used to determine whether the given world chunk-pillar is loaded.
     *
     * @param pos the position of the world chunk-pillar to check.
     * @return whether the given world chunk-pillar is loaded.
     */
    public boolean isWorldChunkLoaded(@NotNull ChunkPos pos) {
        return worldLoadedChunks.contains(pos.toLong());
    }

    /**
     * Increments this timer's time, counting down all chunks. This should be called every server tick.
     */
    public void tick() {
        tickAge++;
    }
}
