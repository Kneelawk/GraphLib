package com.kneelawk.wirenetlib.util;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.List;
import java.util.stream.Collectors;

public class ChunkSectionUnloadTimer extends ChunkUnloadTimer {
    private final int bottomSectionCoord;
    private final int topSectionCoord;

    private final LongSet loadedChunks = new LongOpenHashSet();
    private final Long2LongMap toUnload = new Long2LongLinkedOpenHashMap();

    public ChunkSectionUnloadTimer(int bottomSectionCoord, int topSectionCoord, long maxAge) {
        super(maxAge);
        this.bottomSectionCoord = bottomSectionCoord;
        this.topSectionCoord = topSectionCoord;
    }

    protected void removeUnloadMark(ChunkPos pos) {
        for (int y = bottomSectionCoord; y < topSectionCoord; y++) {
            toUnload.remove(ChunkSectionPos.asLong(pos.x, y, pos.z));
        }
    }

    protected void markForUnloading(ChunkPos pos) {
        for (int y = bottomSectionCoord; y < topSectionCoord; y++) {
            long longPos = ChunkSectionPos.asLong(pos.x, y, pos.z);
            if (loadedChunks.contains(longPos)) {
                toUnload.put(longPos, tickAge + maxAge);
            }
        }
    }

    public void onChunkUse(ChunkSectionPos pos) {
        loadedChunks.add(pos.asLong());
        if (!worldLoadedChunks.contains(pos.toChunkPos().toLong())) {
            toUnload.put(pos.asLong(), tickAge + maxAge);
        }
    }

    public void onChunkUnload(ChunkSectionPos pos) {
        loadedChunks.remove(pos.asLong());
        toUnload.remove(pos.asLong());
    }

    public List<ChunkSectionPos> chunksToUnload() {
        return toUnload.keySet()
                .longStream()
                .filter((longPos) -> toUnload.get(longPos) < tickAge)
                .mapToObj(ChunkSectionPos::from)
                .collect(Collectors.toList());
    }
}
