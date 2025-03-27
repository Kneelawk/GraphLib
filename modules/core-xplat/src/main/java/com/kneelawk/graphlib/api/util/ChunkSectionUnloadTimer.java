package com.kneelawk.graphlib.api.util;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Chunk-Section variant of {@link ChunkUnloadTimer} for keeping track of chunk sections.
 */
public class ChunkSectionUnloadTimer extends ChunkUnloadTimer {
    private final int bottomSectionCoord;
    private final int topSectionCoord;

    private final LongSet loadedChunks = new LongOpenHashSet();
    private final Long2LongMap toUnload = new Long2LongLinkedOpenHashMap();

    /**
     * Constructs a chunk section unload timer.
     *
     * @param bottomSectionCoord the section coordinate of the bottom of the world ({@link Level#getMinSectionY()}).
     * @param topSectionCoord    the section coordinate of the top of the world ({@link Level#getMaxSectionY()}).
     * @param maxAge             the maximum age chunks sections are allowed to be before they're unloaded.
     */
    public ChunkSectionUnloadTimer(int bottomSectionCoord, int topSectionCoord, long maxAge) {
        super(maxAge);
        this.bottomSectionCoord = bottomSectionCoord;
        this.topSectionCoord = topSectionCoord;
    }

    @Override
    protected void removeUnloadMark(@NotNull ChunkPos pos) {
        for (int y = bottomSectionCoord; y < topSectionCoord; y++) {
            toUnload.remove(SectionPos.asLong(pos.x, y, pos.z));
        }
    }

    @Override
    protected void markForUnloading(@NotNull ChunkPos pos) {
        for (int y = bottomSectionCoord; y < topSectionCoord; y++) {
            long longPos = SectionPos.asLong(pos.x, y, pos.z);
            if (loadedChunks.contains(longPos)) {
                toUnload.put(longPos, tickAge + maxAge);
            }
        }
    }

    /**
     * Checks to see if the given chunk section is loaded.
     *
     * @param pos the position of the chunk section to check.
     * @return whether the given chunk section is loaded.
     */
    public boolean isChunkLoaded(@NotNull SectionPos pos) {
        return loadedChunks.contains(pos.asLong());
    }

    /**
     * Resets the given chunk section's unload timer if it is currently counting down to unloading.
     *
     * @param pos the position of the chunk section that was used.
     */
    public void onChunkUse(@NotNull SectionPos pos) {
        loadedChunks.add(pos.asLong());
        if (!worldLoadedChunks.contains(pos.chunk().toLong())) {
            toUnload.put(pos.asLong(), tickAge + maxAge);
        }
    }

    /**
     * Tells this unload timer that the given chunk section has been successfully unloaded and that this should stop
     * tracking it.
     *
     * @param pos the position of the unloaded chunk.
     */
    public void onChunkUnload(@NotNull SectionPos pos) {
        loadedChunks.remove(pos.asLong());
        toUnload.remove(pos.asLong());
    }

    /**
     * Gets a list of the chunk sections to unload this tick.
     *
     * @return a list of chunk sections to unload.
     */
    public List<SectionPos> chunksToUnload() {
        return toUnload.keySet()
            .longStream()
            .filter((longPos) -> toUnload.get(longPos) < tickAge)
            .mapToObj(SectionPos::of)
            .collect(Collectors.toList());
    }
}
