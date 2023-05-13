package com.kneelawk.graphlib.api.util;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;

import net.minecraft.util.math.ChunkPos;

/**
 * Chunk-Pillar variant of {@link ChunkUnloadTimer}, for keeping track of chunk-pillars.
 */
public class ChunkPillarUnloadTimer extends ChunkUnloadTimer {
    private final Long2LongMap toUnload = new Long2LongLinkedOpenHashMap();

    /**
     * Constructs a chunk-pillar unload timer.
     *
     * @param maxAge the maximum age chunk-pillars should be allowed to be before they're unloaded.
     */
    public ChunkPillarUnloadTimer(long maxAge) {
        super(maxAge);
    }

    @Override
    protected void removeUnloadMark(@NotNull ChunkPos pos) {
        toUnload.remove(pos.toLong());
    }

    @Override
    protected void markForUnloading(@NotNull ChunkPos pos) {
        toUnload.put(pos.toLong(), tickAge + maxAge);
    }

    /**
     * Resets the given chunk's unload timer if it is currently counting down to unloading.
     *
     * @param pos the position of the chunk that was used.
     */
    public void onChunkUse(@NotNull ChunkPos pos) {
        long longPos = pos.toLong();
        if (!worldLoadedChunks.contains(longPos)) {
            toUnload.put(longPos, tickAge + maxAge);
        }
    }

    /**
     * Tells this unload timer that the given chunk has been successfully unloaded and should no-longer be tracked.
     *
     * @param pos the position of the unloaded chunk.
     */
    public void onChunkUnload(@NotNull ChunkPos pos) {
        toUnload.remove(pos.toLong());
    }

    /**
     * Gets a list of all the chunks that this unload timer has determined need to be unloaded this tick.
     *
     * @return a list of the chunks to unload.
     */
    public @NotNull List<ChunkPos> chunksToUnload() {
        return toUnload.keySet()
            .longStream()
            .filter((longPos) -> toUnload.get(longPos) < tickAge)
            .mapToObj(ChunkPos::new)
            .collect(Collectors.toList());
    }
}
