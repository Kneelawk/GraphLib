package com.kneelawk.graphlib.api.v1.util;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ChunkPillarUnloadTimer extends ChunkUnloadTimer {
    private final Long2LongMap toUnload = new Long2LongLinkedOpenHashMap();

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

    public void onChunkUse(@NotNull ChunkPos pos) {
        long longPos = pos.toLong();
        if (!worldLoadedChunks.contains(longPos)) {
            toUnload.put(longPos, tickAge + maxAge);
        }
    }

    public void onChunkUnload(@NotNull ChunkPos pos) {
        toUnload.remove(pos.toLong());
    }

    public @NotNull List<ChunkPos> chunksToUnload() {
        return toUnload.keySet()
                .longStream()
                .filter((longPos) -> toUnload.get(longPos) < tickAge)
                .mapToObj(ChunkPos::new)
                .collect(Collectors.toList());
    }
}
