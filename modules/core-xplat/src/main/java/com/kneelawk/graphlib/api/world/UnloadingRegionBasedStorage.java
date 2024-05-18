package com.kneelawk.graphlib.api.world;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

import com.kneelawk.graphlib.api.util.ChunkPillarUnloadTimer;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

/**
 * A region-based storage that unloads chunks after a set time.
 *
 * @param <R> the type of chunk data to store.
 */
public class UnloadingRegionBasedStorage<R> implements RegionBasedStorage<R> {

    /**
     * The max chunk age is 1 minute.
     */
    private static final int MAX_CHUNK_AGE = 20 * 60;
    private static final int INCREMENTAL_SAVE_FACTOR = 10;

    private final ServerLevel world;

    private final Codec<R> sectionCodec;
    private final TrackingChunkFactory<R> createNew;

    private final SaveMode saveMode;

    private final IOWorker worker;

    private final ChunkPillarUnloadTimer timer = new ChunkPillarUnloadTimer(MAX_CHUNK_AGE);

    private final Long2ObjectMap<Int2ObjectMap<R>> loadedChunks = new Long2ObjectOpenHashMap<>();
    private final LongSet unsavedPillars = new LongOpenHashSet();

    private boolean closed = false;

    /**
     * Constructs an unloading region-based-storage.
     *
     * @param storageKey      the key used to describe this storage element when profiling.
     * @param world           the server world this storage is associated with.
     * @param path            the path to where region files should be saved.
     * @param syncChunkWrites whether chunk writes should be written synchronously, corresponding to
     *                        {@link java.nio.file.StandardOpenOption#DSYNC}.
     * @param codec           the chunk section's codec.
     * @param createNew       the function for creating a new, empty chunk section.
     * @param saveMode        how often storage chunks should be saved.
     */
    public UnloadingRegionBasedStorage(@NotNull RegionStorageInfo storageKey, @NotNull ServerLevel world,
                                       @NotNull Path path,
                                       boolean syncChunkWrites, @NotNull Codec<R> codec,
                                       @NotNull TrackingChunkFactory<@NotNull R> createNew,
                                       @NotNull SaveMode saveMode) {
        this.world = world;
        this.sectionCodec = codec;
        this.createNew = createNew;
        this.saveMode = saveMode;
        worker = StorageHelper.newWorker(storageKey, path, syncChunkWrites);
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

    @Override
    public void onWorldChunkLoad(@NotNull ChunkPos pos) {
        if (closed) {
            // ignore chunk loads if we're closed
            return;
        }

        timer.onWorldChunkLoad(pos);
        loadChunkPillar(pos);
    }

    @Override
    public void onWorldChunkUnload(@NotNull ChunkPos pos) {
        timer.onWorldChunkUnload(pos);
    }

    @Override
    public @NotNull R getOrCreate(@NotNull SectionPos pos) {
        ChunkPos chunkPos = pos.chunk();
        timer.onChunkUse(chunkPos);
        long longPos = chunkPos.toLong();
        Int2ObjectMap<R> pillar = loadedChunks.get(longPos);
        if (pillar != null) {
            return pillar.computeIfAbsent(pos.getY(),
                (y) -> createNew(pos, chunkPos));
        } else {
            // try and load the pillar
            pillar = new Int2ObjectOpenHashMap<>();
            try {
                // blocking here isn't great, but often we *need* this data in order to continue
                Optional<CompoundTag> root = worker.loadAsync(chunkPos).join();
                if (root.isPresent()) {
                    loadChunkPillar(chunkPos, pillar, root.get());

                    R section = pillar.get(pos.getY());
                    if (section == null) {
                        section = createNew(pos, chunkPos);
                        pillar.put(pos.getY(), section);
                    }
                    return section;
                } else {
                    R created = createNew(pos, chunkPos);
                    pillar.put(pos.getY(), created);
                    loadedChunks.put(longPos, pillar);
                    return created;
                }
            } catch (Exception e) {
                GLLog.error("Error loading chunk pillar {}. Discarding chunk.", chunkPos, e);

                R created = createNew(pos, chunkPos);
                pillar.put(pos.getY(), created);
                loadedChunks.put(longPos, pillar);
                return created;
            }
        }
    }

    private @NotNull R createNew(@NotNull SectionPos pos, ChunkPos chunkPos) {
        markDirty(chunkPos);
        return createNew.createNew(pos, () -> markDirty(chunkPos));
    }

    @Override
    public @Nullable R getIfExists(@NotNull SectionPos pos) {
        ChunkPos chunkPos = pos.chunk();
        Int2ObjectMap<R> pillar = loadedChunks.get(chunkPos.toLong());
        if (pillar != null) {
            timer.onChunkUse(chunkPos);
            return pillar.get(pos.getY());
        } else {
            // try and load the pillar
            try {
                Optional<CompoundTag> root = worker.loadAsync(chunkPos).join();
                if (root.isPresent()) {
                    timer.onChunkUse(chunkPos);
                    pillar = new Int2ObjectOpenHashMap<>();
                    loadChunkPillar(chunkPos, pillar, root.get());

                    return pillar.get(pos.getY());
                } else {
                    timer.onChunkUse(chunkPos);
                    loadedChunks.put(chunkPos.toLong(), new Int2ObjectOpenHashMap<>());
                    return null;
                }
            } catch (Exception e) {
                GLLog.error("Error loading chunk pillar {}.", chunkPos, e);

                return null;
            }
        }
    }

    private CompletableFuture<Void> loadChunkPillar(@NotNull ChunkPos chunkPos) {
        if (!loadedChunks.containsKey(chunkPos.toLong())) {
            // try and load the pillar
            return worker.loadAsync(chunkPos).thenAcceptAsync(root -> {
                try {
                    // double check that the chunk hasn't already been loaded
                    if (!loadedChunks.containsKey(chunkPos.toLong())) {
                        if (root.isPresent()) {
                            timer.onChunkUse(chunkPos);
                            Int2ObjectMap<R> pillar = new Int2ObjectOpenHashMap<>();
                            loadChunkPillar(chunkPos, pillar, root.get());
                        } else {
                            timer.onChunkUse(chunkPos);
                            loadedChunks.put(chunkPos.toLong(), new Int2ObjectOpenHashMap<>());
                        }
                    }
                } catch (Exception e) {
                    GLLog.error("Error loading chunk pillar {}.", chunkPos, e);
                }
            }, world.getServer());
        }
        return CompletableFuture.completedFuture(null);
    }

    private void loadChunkPillar(@NotNull ChunkPos chunkPos, @NotNull Int2ObjectMap<R> pillar,
                                 @NotNull CompoundTag root) {
        // TODO: move this over to dynamics and DFU fix it
        CompoundTag sectionsTag = root.getCompound("Sections");
        for (int sectionY = world.getMinSection(); sectionY < world.getMaxSection(); sectionY++) {
            if (sectionsTag.contains(String.valueOf(sectionY), Tag.TAG_COMPOUND)) {
                CompoundTag sectionTag = sectionsTag.getCompound(String.valueOf(sectionY));
                try {
                    DynamicOps<Tag> ops = createOps(SectionPos.of(chunkPos, sectionY), () -> markDirty(chunkPos));
                    DataResult<R> res = sectionCodec.parse(ops, sectionTag);
                    final int y = sectionY;
                    Optional<R> opt = res.resultOrPartial(
                        err -> GLLog.error("Error loading chunk {}, section {}: {}", chunkPos, y, err));

                    if (opt.isPresent()) {
                        R section = opt.get();
                        pillar.put(sectionY, section);
                    } else {
                        GLLog.error("Unable to load chunk {}, section {} due to previous errors.", chunkPos, sectionY);
                    }
                } catch (Exception e) {
                    GLLog.error("Error loading chunk {} section {}. Discarding chunk section.", chunkPos, sectionY, e);
                }
            }
        }

        loadedChunks.put(chunkPos.toLong(), pillar);
    }

    private void markDirty(ChunkPos pos) {
        unsavedPillars.add(pos.toLong());
    }

    @Override
    public void tick() {
        timer.tick();

        for (ChunkPos pos : timer.chunksToUnload()) {
            if (unsavedPillars.contains(pos.toLong())) {
                saveChunk(pos);
            }
            unsavedPillars.remove(pos.toLong());
            loadedChunks.remove(pos.toLong());
            timer.onChunkUnload(pos);
        }

        if (!unsavedPillars.isEmpty() && (saveMode == SaveMode.INCREMENTAL || saveMode == SaveMode.IMMEDIATE)) {
            int saveCount;
            if (saveMode == SaveMode.IMMEDIATE) {
                saveCount = unsavedPillars.size();
            } else {
                saveCount = (unsavedPillars.size() + INCREMENTAL_SAVE_FACTOR - 1) / INCREMENTAL_SAVE_FACTOR;
            }

            LongIterator iter = unsavedPillars.longIterator();
            while (iter.hasNext() && saveCount > 0) {
                ChunkPos pos = new ChunkPos(iter.nextLong());
                saveChunk(pos);
                iter.remove();
                saveCount--;
            }
        }
    }

    @Override
    public void saveAll() {
        for (long key : loadedChunks.keySet()) {
            saveChunk(new ChunkPos(key));
        }
    }

    @Override
    public void saveChunk(@NotNull ChunkPos pos) {
        Int2ObjectMap<R> sections = loadedChunks.get(pos.toLong());
        if (sections != null && !sections.isEmpty()) {
            CompoundTag root = new CompoundTag();

            CompoundTag sectionsTag = new CompoundTag();
            for (int sectionY = world.getMinSection(); sectionY < world.getMaxSection(); sectionY++) {
                R section = sections.get(sectionY);
                if (section != null) {
                    try {
                        DynamicOps<Tag> ops = createOps(SectionPos.of(pos, sectionY), () -> {});
                        DataResult<Tag> nbtRes = sectionCodec.encodeStart(ops, section);
                        final int y = sectionY;
                        Optional<Tag> nbtOpt = nbtRes.resultOrPartial(
                            err -> GLLog.error("Error saving chunk {}, section {}: {}", pos, y, err));

                        if (nbtOpt.isPresent()) {
                            sectionsTag.put(String.valueOf(sectionY), nbtOpt.get());
                        } else {
                            GLLog.error("Unable to save chunk {}, section {} due to previous errors.", pos, sectionY);
                        }
                    } catch (Exception e) {
                        GLLog.error("Error saving chunk {}, section {}", pos, sectionY, e);
                    }
                }
            }
            root.put("Sections", sectionsTag);

            worker.store(pos, root);
        } else {
            worker.store(pos, null);
        }
    }

    private DynamicOps<Tag> createOps(SectionPos pos, Runnable markDirty) {
        DynamicOps<Tag> ops = NbtOps.INSTANCE;
        ops = world.registryAccess().createSerializationContext(ops);
        ops = SECTION_POS.push(ops, pos);
        ops = MARK_DIRTY.push(ops, markDirty);
        return ops;
    }
}
