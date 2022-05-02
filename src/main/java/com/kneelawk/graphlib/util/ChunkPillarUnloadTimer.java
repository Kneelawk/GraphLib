package com.kneelawk.graphlib.util;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.stream.Collectors;

public class ChunkPillarUnloadTimer extends ChunkUnloadTimer {
    private final Long2LongMap toUnload = new Long2LongLinkedOpenHashMap();

    public ChunkPillarUnloadTimer(long maxAge) {
        super(maxAge);
    }

    @Override
    protected void removeUnloadMark(ChunkPos pos) {
        toUnload.remove(pos.toLong());
    }

    @Override
    protected void markForUnloading(ChunkPos pos) {
        toUnload.put(pos.toLong(), tickAge + maxAge);
    }

    public void onChunkUse(ChunkPos pos) {
        long longPos = pos.toLong();
        if (!worldLoadedChunks.contains(longPos)) {
            toUnload.put(longPos, tickAge + maxAge);
        }
    }

    public void onChunkUnload(ChunkPos pos) {
        toUnload.remove(pos.toLong());
    }

    public List<ChunkPos> chunksToUnload() {
        return toUnload.keySet()
                .longStream()
                .filter((longPos) -> toUnload.get(longPos) < tickAge)
                .mapToObj(ChunkPos::new)
                .collect(Collectors.toList());
    }
}
