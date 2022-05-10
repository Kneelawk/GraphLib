package com.kneelawk.graphlib.world;

import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.mixin.api.StorageHelper;
import com.kneelawk.graphlib.util.ChunkPillarUnloadTimer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UnloadingRegionBasedStorage<R extends StorageChunk> implements AutoCloseable {
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

    private boolean closed = false;

    public UnloadingRegionBasedStorage(@NotNull ServerWorld world, @NotNull Path path, boolean syncChunkWrites,
                                       @NotNull BiFunction<@NotNull NbtCompound, @NotNull ChunkSectionPos, @NotNull R> loadFromNbt,
                                       @NotNull Function<@NotNull ChunkSectionPos, @NotNull R> createNew) {
        this.world = world;
        this.loadFromNbt = loadFromNbt;
        this.createNew = createNew;
        worker = StorageHelper.newWorker(path, syncChunkWrites, path.getFileName().toString());
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        closed = true;

        saveAll();

        worker.close();
    }

    public void onWorldChunkLoad(@NotNull ChunkPos pos) {
        if (closed) {
            // ignore chunk loads if we're closed
            return;
        }

        timer.onWorldChunkLoad(pos);
        loadChunkPillar(pos);
    }

    public void onWorldChunkUnload(@NotNull ChunkPos pos) {
        timer.onWorldChunkUnload(pos);
    }

    public @NotNull R getOrCreate(@NotNull ChunkSectionPos pos) {
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
                    loadChunkPillar(chunkPos, pillar, root);

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
                GraphLib.log.error("Error loading chunk pillar {}. Discarding chunk.", chunkPos, e);

                R created = createNew.apply(pos);
                pillar.put(pos.getY(), created);
                loadedChunks.put(longPos, pillar);
                return created;
            }
        }
    }

    @Nullable
    public R getIfExists(@NotNull ChunkSectionPos pos) {
        ChunkPos chunkPos = pos.toChunkPos();
        Int2ObjectMap<R> pillar = loadedChunks.get(chunkPos.toLong());
        if (pillar != null) {
            timer.onChunkUse(chunkPos);
            return pillar.get(pos.getY());
        } else {
            // try and load the pillar
            try {
                NbtCompound root = worker.getNbt(chunkPos);
                if (root != null) {
                    timer.onChunkUse(chunkPos);
                    pillar = new Int2ObjectOpenHashMap<>();
                    loadChunkPillar(chunkPos, pillar, root);

                    return pillar.get(pos.getY());
                } else {
                    return null;
                }
            } catch (Exception e) {
                GraphLib.log.error("Error loading chunk pillar {}.", chunkPos, e);

                return null;
            }
        }
    }

    private void loadChunkPillar(@NotNull ChunkPos chunkPos) {
        if (!loadedChunks.containsKey(chunkPos.toLong())) {
            // try and load the pillar
            try {
                NbtCompound root = worker.getNbt(chunkPos);
                if (root != null) {
                    timer.onChunkUse(chunkPos);
                    Int2ObjectMap<R> pillar = new Int2ObjectOpenHashMap<>();
                    loadChunkPillar(chunkPos, pillar, root);
                }
            } catch (Exception e) {
                GraphLib.log.error("Error loading chunk pillar {}.", chunkPos, e);
            }
        }
    }

    private void loadChunkPillar(@NotNull ChunkPos chunkPos, @NotNull Int2ObjectMap<R> pillar,
                                 @NotNull NbtCompound root) {
        NbtCompound sectionsTag = root.getCompound("Sections");
        for (int sectionY = world.getBottomSectionCoord();
             sectionY < world.getTopSectionCoord(); sectionY++) {
            if (sectionsTag.contains(String.valueOf(sectionY), NbtElement.COMPOUND_TYPE)) {
                NbtCompound sectionTag = sectionsTag.getCompound(String.valueOf(sectionY));
                try {
                    R section = loadFromNbt.apply(sectionTag,
                            ChunkSectionPos.from(chunkPos.x, sectionY, chunkPos.z));
                    pillar.put(sectionY, section);
                } catch (Exception e) {
                    GraphLib.log.error("Error loading chunk {} section {}. Discarding chunk section.",
                            chunkPos, sectionY, e);
                }
            }
        }

        loadedChunks.put(chunkPos.toLong(), pillar);
    }

    public void tick() {
        timer.tick();

        for (ChunkPos pos : timer.chunksToUnload()) {
            saveChunk(pos);
            loadedChunks.remove(pos.toLong());
            timer.onChunkUnload(pos);
        }
    }

    public void saveAll() {
        for (long key : loadedChunks.keySet()) {
            saveChunk(new ChunkPos(key));
        }
    }

    public void saveChunk(@NotNull ChunkPos pos) {
        Int2ObjectMap<R> sections = loadedChunks.get(pos.toLong());
        if (sections != null) {
            NbtCompound root = new NbtCompound();

            NbtCompound sectionsTag = new NbtCompound();
            for (int sectionY = world.getBottomSectionCoord(); sectionY < world.getTopSectionCoord(); sectionY++) {
                R section = sections.get(sectionY);
                if (section != null) {
                    try {
                        NbtCompound nbt = new NbtCompound();
                        section.toNbt(nbt);
                        sectionsTag.put(String.valueOf(sectionY), nbt);
                    } catch (Exception e) {
                        GraphLib.log.error("Error saving chunk {}, section {}", pos, sectionY, e);
                    }
                }
            }
            root.put("Sections", sectionsTag);

            worker.setResult(pos, root);
        }
    }
}
