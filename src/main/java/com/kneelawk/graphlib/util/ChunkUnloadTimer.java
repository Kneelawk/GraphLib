package com.kneelawk.graphlib.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;

public abstract class ChunkUnloadTimer {
    protected final long maxAge;
    protected long tickAge;

    protected final LongSet worldLoadedChunks = new LongOpenHashSet();

    public ChunkUnloadTimer(long maxAge) {
        this.maxAge = maxAge;
        tickAge = 0;
    }

    public void onWorldChunkLoad(ChunkPos pos) {
        worldLoadedChunks.add(pos.toLong());
        removeUnloadMark(pos);
    }

    protected abstract void removeUnloadMark(ChunkPos pos);

    public void onWorldChunkUnload(ChunkPos pos) {
        worldLoadedChunks.remove(pos.toLong());
        markForUnloading(pos);
    }

    protected abstract void markForUnloading(ChunkPos pos);

    public boolean isWorldChunkLoad(ChunkPos pos) {
        return worldLoadedChunks.contains(pos.toLong());
    }

    public void tick() {
        tickAge++;
    }
}
