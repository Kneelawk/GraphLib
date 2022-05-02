package com.kneelawk.graphlib.world;

import com.kneelawk.graphlib.GraphLibMod;
import com.kneelawk.graphlib.mixin.api.StorageHelper;
import com.kneelawk.graphlib.util.ChunkPillarUnloadTimer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.storage.StorageIoWorker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class UnloadingRegionBasedStorage<R extends StorageChunk> implements AutoCloseable {
    /**
     * The max chunk age is 1 minute.
     */
    private static final int MAX_CHUNK_AGE = 20 * 60;

    private final ServerWorld world;

    private final BiFunction<NbtCompound, ChunkSectionPos, R> loadFromNbt;
    private final Function<ChunkSectionPos, R> createNew;

    private final StorageIoWorker worker;

    private final ChunkPillarUnloadTimer timer = new ChunkPillarUnloadTimer(MAX_CHUNK_AGE);

    private final Long2ObjectMap<Int2ObjectMap<R>> loadedChunks = new Long2ObjectOpenHashMap<>();

    public UnloadingRegionBasedStorage(ServerWorld world, Path path, boolean syncChunkWrites,
                                       BiFunction<NbtCompound, ChunkSectionPos, R> loadFromNbt,
                                       Function<ChunkSectionPos, R> createNew) {
        this.world = world;
        this.loadFromNbt = loadFromNbt;
        this.createNew = createNew;
        worker = StorageHelper.newWorker(path, syncChunkWrites, path.getFileName().toString());
    }

    @Override
    public void close() throws IOException {
        worker.close();
    }

    protected boolean isWorldChunkLoaded(ChunkPos pos) {
        return timer.isWorldChunkLoad(pos);
    }

    public void onWorldChunkLoad(ChunkPos pos) {
        timer.onWorldChunkLoad(pos);
    }

    public void onWorldChunkUnload(ChunkPos pos) {
        timer.onWorldChunkUnload(pos);
    }

    public R getOrCreate(ChunkSectionPos pos) {
        ChunkPos chunkPos = pos.toChunkPos();
        timer.onChunkUse(chunkPos);
        long longPos = chunkPos.toLong();
        Int2ObjectMap<R> pillar = loadedChunks.get(longPos);
        if (pillar != null) {
            return pillar.computeIfAbsent(pos.getY(), (y) -> createNew.apply(pos));
        } else {
            // try and load the pillar
            pillar = new Int2ObjectOpenHashMap<>();
            try {
                NbtCompound root = worker.getNbt(chunkPos);
                if (root != null) {
                    for (int sectionY = world.getBottomSectionCoord();
                         sectionY < world.getTopSectionCoord(); sectionY++) {
                        NbtCompound sectionTag = root.getCompound(String.valueOf(sectionY));
                        if (sectionTag != null) {
                            try {
                                R section = loadFromNbt.apply(sectionTag,
                                        ChunkSectionPos.from(pos.getX(), sectionY, pos.getZ()));
                                pillar.put(sectionY, section);
                            } catch (Exception e) {
                                GraphLibMod.log.error("Error loading chunk {} section {}. Discarding chunk section.",
                                        chunkPos, sectionY, e);
                            }
                        }
                    }

                    loadedChunks.put(longPos, pillar);

                    R section = pillar.get(pos.getY());
                    if (section == null) {
                        section = createNew.apply(pos);
                        pillar.put(pos.getY(), section);
                    }
                    return section;
                } else {
                    R created = createNew.apply(pos);
                    pillar.put(pos.getY(), created);
                    loadedChunks.put(longPos, pillar);
                    return created;
                }
            } catch (Exception e) {
                GraphLibMod.log.error("Error loading chunk pillar {}. Discarding chunk.", chunkPos, e);

                R created = createNew.apply(pos);
                pillar.put(pos.getY(), created);
                loadedChunks.put(longPos, pillar);
                return created;
            }
        }
    }

    public void tick() {
        timer.tick();

        for (ChunkPos pos : timer.chunksToUnload()) {
            saveChunk(pos);
            loadedChunks.remove(pos.toLong());
            timer.onChunkUnload(pos);
        }
    }

    public void saveChunk(ChunkPos pos) {
        if (!loadedChunks.isEmpty()) {
            NbtCompound root = new NbtCompound();

            NbtCompound sectionsTag = new NbtCompound();
            Int2ObjectMap<R> sections = loadedChunks.get(pos.toLong());
            if (sections != null) {
                for (int sectionY = world.getBottomSectionCoord(); sectionY < world.getTopSectionCoord(); sectionY++) {
                    R section = sections.get(sectionY);
                    if (section != null) {
                        try {
                            NbtCompound nbt = new NbtCompound();
                            section.toNbt(nbt);
                            sectionsTag.put(String.valueOf(sectionY), nbt);
                        } catch (Exception e) {
                            GraphLibMod.log.error("Error saving chunk {}, section {}", pos, sectionY, e);
                        }
                    }
                }
            }
            root.put("Sections", sectionsTag);

            worker.setResult(pos, root);
        }
    }
}
