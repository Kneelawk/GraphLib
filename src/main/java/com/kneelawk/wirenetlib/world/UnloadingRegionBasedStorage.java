package com.kneelawk.wirenetlib.world;

import com.kneelawk.wirenetlib.mixin.api.StorageHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;

import java.io.IOException;
import java.nio.file.Path;

public abstract class UnloadingRegionBasedStorage<R extends StorageChunk> implements AutoCloseable {
    private final StorageIoWorker worker;

    private final LongSet worldLoadeChunks = new LongOpenHashSet();

    private final Long2ObjectMap<R> loadedChunks = new Long2ObjectOpenHashMap<>();

    public UnloadingRegionBasedStorage(Path path, boolean syncChunkWrites) {
        worker = StorageHelper.newWorker(path, syncChunkWrites, path.getFileName().toString());
    }

    @Override
    public void close() throws IOException {
        worker.close();
    }

    public void onChunkLoad(ChunkPos pos) {
        worldLoadeChunks.add(pos.toLong());
    }

    public void onChunkUnload(ChunkPos pos) {
        worldLoadeChunks.remove(pos.toLong());
    }

    public void tick() {

    }

    public void saveChunk(ChunkPos pos) {

    }
}
