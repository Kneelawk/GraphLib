package com.kneelawk.graphlib.api.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

public abstract class ChunkUnloadTimer {
    protected final long maxAge;
    protected long tickAge;

    protected final LongSet worldLoadedChunks = new LongOpenHashSet();

    public ChunkUnloadTimer(long maxAge) {
        this.maxAge = maxAge;
        tickAge = 0;
    }

    public void onWorldChunkLoad(@NotNull ChunkPos pos) {
        worldLoadedChunks.add(pos.toLong());
        removeUnloadMark(pos);
    }

    protected abstract void removeUnloadMark(@NotNull ChunkPos pos);

    public void onWorldChunkUnload(@NotNull ChunkPos pos) {
        worldLoadedChunks.remove(pos.toLong());
        markForUnloading(pos);
    }

    protected abstract void markForUnloading(@NotNull ChunkPos pos);

    public boolean isWorldChunkLoad(@NotNull ChunkPos pos) {
        return worldLoadedChunks.contains(pos.toLong());
    }

    public void tick() {
        tickAge++;
    }
}
