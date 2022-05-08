package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.ChunkSectionUnloadTimer;
import com.kneelawk.graphlib.world.UnloadingRegionBasedStorage;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BlockGraphController implements AutoCloseable {
    /**
     * Graphs will unload 1 minute after their chunk unloads or their last use.
     */
    private static final int MAX_AGE = 20 * 60;

    private final ServerWorld world;

    private final UnloadingRegionBasedStorage<BlockGraphChunk> chunks;

    private final ChunkSectionUnloadTimer timer;

    private final Path graphsDir;

    private final Path stateFile;

    private final Long2ObjectMap<BlockGraph> loadedGraphs = new Long2ObjectLinkedOpenHashMap<>();

    private final ObjectSet<Node<BlockNodeWrapper<?>>> toUpdate = new ObjectLinkedOpenHashSet<>();

    private boolean stateDirty = false;
    private long nextGraphId = 0L;

    public BlockGraphController(ServerWorld world, Path path, boolean syncChunkWrites) {
        this.chunks = new UnloadingRegionBasedStorage<>(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites,
                BlockGraphChunk::new, BlockGraphChunk::new);
        this.world = world;
        graphsDir = path.resolve(Constants.GRAPHS_DIRNAME);
        stateFile = path.resolve(Constants.STATE_FILENAME);
        timer = new ChunkSectionUnloadTimer(world.getBottomSectionCoord(), world.getTopSectionCoord(), MAX_AGE);

        loadState();
    }

    // ---- Lifecycle Methods ---- //

    public void onWorldChunkLoad(ChunkPos pos) {
        chunks.onWorldChunkLoad(pos);
        timer.onWorldChunkLoad(pos);

        loadGraphs(pos);
    }

    public void onWorldChunkUnload(ChunkPos pos) {
        chunks.onWorldChunkUnload(pos);
        timer.onWorldChunkUnload(pos);
    }

    public void tick() {
        chunks.tick();
        timer.tick();

        handleUpdates();
        unloadGraphs();
    }

    public void saveChunk(ChunkPos pos) {
        chunks.saveChunk(pos);
        saveState();
    }

    @Override
    public void close() throws Exception {
        chunks.close();

        // handle any pending updates before we shut down, cause that stuff can't be saved
        handleUpdates();

        saveAllGraphs();
        saveState();
    }

    // ---- Public Interface Methods ---- //

    public Stream<Node<BlockNodeWrapper<?>>> getNodesAt(BlockPos pos) {
        BlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos));
        if (chunk != null) {
            return chunk.graphsInPos.get(ChunkSectionPos.packLocal(pos)).longStream().mapToObj(this::getGraph)
                    .filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
        } else {
            return Stream.empty();
        }
    }

    @Nullable
    public BlockGraph getGraph(long id) {
        BlockGraph graph = loadedGraphs.get(id);
        if (graph == null) {
            graph = readGraph(id);
            if (graph != null) {
                loadedGraphs.put(id, graph);
            }
        }

        if (graph != null) {
            for (long posLong : graph.chunks) {
                timer.onChunkUse(ChunkSectionPos.from(posLong));
            }
        }

        return graph;
    }

    public BlockGraph createGraph() {
        BlockGraph graph = new BlockGraph(this, getNextGraphId());
        loadedGraphs.put(graph.getId(), graph);
        return graph;
    }

    public void destroyGraph(long id) {
        BlockGraph graph = getGraph(id);
        if (graph == null) {
            // The graph does not exist.
            GraphLib.log.warn("Attempted to destroy graph that does not exist. Id: {}", id);
            return;
        }

        loadedGraphs.remove(id);
        try {
            Files.deleteIfExists(getGraphFile(id));
        } catch (IOException e) {
            GraphLib.log.error("Error removing graph file. Id: {}", id, e);
        }

        for (long sectionPos : graph.chunks) {
            BlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(sectionPos));
            if (chunk != null) {
                // Note: if this is changed to only remove from block-poses that the graph actually occupies, make sure
                // not to get those block-poses from the block-graph's graph, because the block-graph's graph will often
                // already have been cleared by the time this function is called.
                chunk.removeGraph(id);
            } else {
                GraphLib.log.warn("Attempted to destroy graph in chunk that does not exist. Id: {}, chunk: {}", id,
                        ChunkSectionPos.from(sectionPos));
            }
        }
    }

    // ---- Internal Methods ---- //

    void addGraphInPos(long id, BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        BlockGraphChunk chunk = chunks.getOrCreate(sectionPos);
        chunk.addGraphInPos(id, pos);

        timer.onChunkUse(sectionPos);
    }

    void removeGraphInPos(long id, BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        BlockGraphChunk chunk = chunks.getIfExists(sectionPos);
        if (chunk != null) {
            short local = ChunkSectionPos.packLocal(pos);
            LongSet graphsInPos = chunk.graphsInPos.get(local);
            graphsInPos.remove(id);
            if (graphsInPos.isEmpty()) {
                chunk.graphsInPos.remove(local);
            }
        } else {
            GraphLib.log.warn("Tried to remove graph from non-existent chunk. Id: {}, chunk: {}, block: {}", id,
                    sectionPos, pos);
        }
    }

    void removeGraphInChunk(long id, long pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        BlockGraphChunk chunk = chunks.getIfExists(sectionPos);
        if (chunk != null) {
            chunk.graphsInChunk.remove(id);
        } else {
            GraphLib.log.warn("Tried to remove graph fom non-existent chunk. Id: {}, chunk: {}", id, sectionPos);
        }
    }

    void removeGraphInPoses(long id, Iterable<BlockPos> poses, LongIterable chunkPoses) {
        for (BlockPos pos : poses) {
            removeGraphInPos(id, pos);
        }
        for (long pos : chunkPoses) {
            removeGraphInChunk(id, pos);
        }
    }

    // ---- Private Methods ---- //

    void scheduleUpdate(Node<BlockNodeWrapper<?>> node) {
        toUpdate.add(node);
    }

    private void handleUpdates() {
        for (var node : toUpdate) {
            BlockNodeWrapper<?> data = node.data();
            data.node().onChanged(world, data.pos());
        }
    }

    private void loadGraphs(ChunkPos pos) {
        for (int y = world.getBottomSectionCoord(); y < world.getTopSectionCoord(); y++) {
            BlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos.x, y, pos.z));
            if (chunk != null) {
                for (long id : chunk.graphsInChunk) {
                    getGraph(id);
                }
            }
        }
    }

    private void unloadGraphs() {
        List<ChunkSectionPos> chunksToUnload = timer.chunksToUnload();
        for (ChunkSectionPos chunk : chunksToUnload) {
            // acknowledge that we're unloading the chunk's data
            timer.onChunkUnload(chunk);
        }

        if (!chunksToUnload.isEmpty()) {
            LongSet toUnload = new LongLinkedOpenHashSet();

            for (BlockGraph graph : loadedGraphs.values()) {
                // we want to only unload graphs that aren't in any loaded chunks
                if (graph.chunks.longStream().mapToObj(ChunkSectionPos::from).noneMatch(timer::isChunkLoaded)) {
                    toUnload.add(graph.getId());
                }
            }

            for (long id : toUnload) {
                // unload the graphs
                BlockGraph graph = loadedGraphs.remove(id);
                writeGraph(graph);
            }
        }
    }

    private void saveAllGraphs() {
        for (BlockGraph graph : loadedGraphs.values()) {
            writeGraph(graph);
        }
    }

    private long getNextGraphId() {
        do {
            nextGraphId++;
        } while (graphExists(nextGraphId));
        markStateDirty();
        return nextGraphId;
    }

    private boolean graphExists(long id) {
        return loadedGraphs.containsKey(id) || Files.exists(getGraphFile(id));
    }

    private Path getGraphFile(long id) {
        return graphsDir.resolve(String.format("%016X.dat", id));
    }

    private void writeGraph(BlockGraph graph) {
        Path graphFile = getGraphFile(graph.getId());

        NbtCompound root = new NbtCompound();
        root.put("data", graph.toTag());

        try (OutputStream os = Files.newOutputStream(graphFile)) {
            NbtIo.writeCompressed(root, os);
        } catch (IOException e) {
            GraphLib.log.error("Unable to save graph {}.", graph.getId(), e);
        }
    }

    @Nullable
    private BlockGraph readGraph(long id) {
        Path graphFile = getGraphFile(id);

        if (!Files.exists(graphFile)) {
            return null;
        }

        try (InputStream is = Files.newInputStream(graphFile)) {
            NbtCompound root = NbtIo.readCompressed(is);
            NbtCompound data = root.getCompound("data");
            return BlockGraph.fromTag(this, id, data);
        } catch (IOException e) {
            GraphLib.log.error("Unable to load graph {}.", id, e);
            return null;
        }
    }

    private void markStateDirty() {
        stateDirty = true;
    }

    private void loadState() {
        if (Files.exists(stateFile)) {
            try (InputStream is = Files.newInputStream(stateFile)) {
                NbtCompound root = NbtIo.readCompressed(is);
                NbtCompound data = root.getCompound("data");
                nextGraphId = data.getLong("nextGraphId");
            } catch (Exception e) {
                GraphLib.log.error("Error loading graph controller state file.", e);
            }
        }
    }

    private void saveState() {
        if (stateDirty) {
            NbtCompound root = new NbtCompound();

            NbtCompound data = new NbtCompound();
            data.putLong("nextGraphId", nextGraphId);

            root.put("data", data);

            if (!Files.exists(stateFile.getParent())) {
                try {
                    Files.createDirectories(stateFile.getParent());
                } catch (IOException e) {
                    GraphLib.log.error("Error creating graph controller state save directory.", e);
                }
            }

            try (OutputStream os = Files.newOutputStream(stateFile)) {
                NbtIo.writeCompressed(root, os);
            } catch (IOException e) {
                GraphLib.log.error("Error saving graph controller state.", e);
                return;
            }

            stateDirty = false;
        }
    }
}
