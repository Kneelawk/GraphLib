package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.ChunkSectionUnloadTimer;
import com.kneelawk.graphlib.util.SidedPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Holds and manages all block graphs for a given world.
 */
public class BlockGraphController implements AutoCloseable, NodeView {
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
    private long prevGraphId = -1L;

    private boolean closed = false;

    public BlockGraphController(@NotNull ServerWorld world, @NotNull Path path, boolean syncChunkWrites) {
        this.chunks = new UnloadingRegionBasedStorage<>(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites,
                BlockGraphChunk::new, BlockGraphChunk::new);
        this.world = world;
        graphsDir = path.resolve(Constants.GRAPHS_DIRNAME);
        stateFile = path.resolve(Constants.STATE_FILENAME);
        timer = new ChunkSectionUnloadTimer(world.getBottomSectionCoord(), world.getTopSectionCoord(), MAX_AGE);

        try {
            Files.createDirectories(graphsDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create graphs dir: '" + graphsDir + "'. This is a fatal exception.",
                    e);
        }

        loadState();
    }

    // ---- Lifecycle Methods ---- //

    public void onWorldChunkLoad(@NotNull ChunkPos pos) {
        if (closed) {
            // Ignore chunk loads if we're closed.
            // In case something decides to try and load a chunk while saving data :/
            return;
        }

        chunks.onWorldChunkLoad(pos);
        timer.onWorldChunkLoad(pos);

        loadGraphs(pos);
    }

    public void onWorldChunkUnload(@NotNull ChunkPos pos) {
        chunks.onWorldChunkUnload(pos);
        timer.onWorldChunkUnload(pos);
    }

    public void tick() {
        chunks.tick();
        timer.tick();

        handleUpdates();
        unloadGraphs();
    }

    public void saveChunk(@NotNull ChunkPos pos) {
        saveState();
        saveGraphs(pos);
        chunks.saveChunk(pos);
    }

    public void saveAll() {
        GraphLib.log.info("Saving block-graph for '{}'/{}", world, world.getRegistryKey().getValue());

        saveAllGraphs();
        saveState();

        chunks.saveAll();
    }

    @Override
    public void close() throws Exception {
        if (closed) {
            return;
        }

        closed = true;

        // Handle any pending updates before we shut down, cause that stuff can't be saved.
        // Note: This may cause chunk loads. I might need to remove this.
        handleUpdates();

        saveAllGraphs();
        saveState();

        chunks.close();
    }

    // ---- Public Interface Methods ---- //

    /**
     * Gets all nodes in the given block-position.
     *
     * @param pos the block-position to get nodes in.
     * @return a stream of the nodes in the given block-position.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull BlockPos pos) {
        // no need for a .distict() here, because you should never have the same node be part of multiple graphs
        return getGraphsInPos(pos).mapToObj(this::getGraph).filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
    }

    /**
     * Gets all nodes in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of the nodes in the given sided block-position.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull SidedPos pos) {
        return getGraphsInPos(pos.pos()).mapToObj(this::getGraph).filter(Objects::nonNull)
                .flatMap(g -> g.getNodesAt(pos));
    }

    /**
     * Gets the IDs of all graph with nodes in the given block-position.
     *
     * @param pos the block-position to get the IDs of graphs with nodes at.
     * @return a stream of all the IDs of graphs with nodes in the given block-position.
     */
    public @NotNull LongStream getGraphsInPos(@NotNull BlockPos pos) {
        BlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos));
        if (chunk != null) {
            LongSet graphsInPos = chunk.graphsInPos.get(ChunkSectionPos.packLocal(pos));
            if (graphsInPos != null) {
                return graphsInPos.longStream();
            } else {
                return LongStream.empty();
            }
        } else {
            return LongStream.empty();
        }
    }

    /**
     * Notifies the controller that a block-position has been changed and may need to have its nodes and connections
     * recalculated.
     *
     * @param pos the changed block-position.
     */
    public void onChanged(@NotNull BlockPos pos) {
        Set<BlockNode> nodes = GraphLib.getNodesInBlock(world, pos);
        onNodesChanged(pos, nodes);
    }

    /**
     * Notifies the controller that a list of block-positions have been changed and may need to have their nodes and
     * connections recalculated.
     *
     * @param poses the iterable of all the block-positions that might have been changed.
     */
    public void onChanged(@NotNull Iterable<BlockPos> poses) {
        for (BlockPos pos : poses) {
            // I couldn't figure out how to optimise this much, so I'm just calling onChanged for every block-pos
            onChanged(pos);
        }
    }

    /**
     * Updates the connections for all the nodes at the given block-position.
     *
     * @param pos the block-position of the nodes to update connections for.
     */
    public void updateConnections(@NotNull BlockPos pos) {
        for (var node : getNodesAt(pos).toList()) {
            updateNodeConnections(node);
        }
    }

    /**
     * Updates the connections for all the nodes at the given sided block-position.
     *
     * @param pos the sided block-position of the nodes to update connections for.
     */
    public void updateConnections(@NotNull SidedPos pos) {
        for (var node : getNodesAt(pos).toList()) {
            updateNodeConnections(node);
        }
    }

    /**
     * Gets the graph with the given ID.
     *
     * @param id the ID of the graph to get.
     * @return the graph with the given ID.
     */
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

    /**
     * Creates a new graph and stores it, assigning it an ID.
     *
     * @return the newly-created graph.
     */
    public @NotNull BlockGraph createGraph() {
        BlockGraph graph = new BlockGraph(this, getNextGraphId());
        loadedGraphs.put(graph.getId(), graph);
        return graph;
    }

    /**
     * Deletes a graph and all nodes it contains.
     *
     * @param id the ID of the graph to delete.
     */
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

    void addGraphInPos(long id, @NotNull BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        BlockGraphChunk chunk = chunks.getOrCreate(sectionPos);
        chunk.addGraphInPos(id, pos);

        timer.onChunkUse(sectionPos);
    }

    void removeGraphInPos(long id, @NotNull BlockPos pos) {
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

    void removeGraphInPoses(long id, @NotNull Iterable<BlockPos> poses, @NotNull LongIterable chunkPoses) {
        for (BlockPos pos : poses) {
            removeGraphInPos(id, pos);
        }
        for (long pos : chunkPoses) {
            removeGraphInChunk(id, pos);
        }
    }

    // ---- Node Update Methods ---- //

    void scheduleUpdate(@NotNull Node<BlockNodeWrapper<?>> node) {
        toUpdate.add(node);
    }

    private void handleUpdates() {
        for (var node : toUpdate) {
            BlockNodeWrapper<?> data = node.data();
            data.node().onChanged(world, data.pos());
        }
        toUpdate.clear();
    }

    // ---- Private Methods ---- //

    private void onNodesChanged(@NotNull BlockPos pos, @NotNull Set<BlockNode> nodes) {
        Set<BlockNode> newNodes = new LinkedHashSet<>(nodes);

        for (long graphId : getGraphsInPos(pos).toArray()) {
            BlockGraph graph = getGraph(graphId);
            if (graph == null) {
                GraphLib.log.warn("Encountered invalid graph in position when detecting node changes. Id: {}, pos: {}",
                        graphId, pos);
                continue;
            }

            for (var node : graph.getNodesAt(pos).toList()) {
                BlockNode bn = node.data().node();
                if (!nodes.contains(bn)) {
                    graph.destroyNode(node);
                }
                newNodes.remove(bn);
            }
        }

        for (BlockNode bn : newNodes) {
            BlockGraph newGraph = createGraph();
            Node<BlockNodeWrapper<?>> node = newGraph.createNode(pos, bn);
            updateNodeConnections(node);
        }
    }

    private void updateNodeConnections(@NotNull Node<BlockNodeWrapper<?>> node) {
        long nodeGraphId = node.data().graphId;
        BlockGraph graph = getGraph(nodeGraphId);

        if (graph == null) {
            GraphLib.log.warn("Tried to update node with invalid graph id. Node: {}", node);
            return;
        }

        var oldConnections = node.connections().stream().map(link -> link.other(node))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var wantedConnections = new LinkedHashSet<>(node.data().node().findConnections(world, this, node.data().pos()));
        wantedConnections.removeIf(other -> !other.data().node().canConnect(world, this, other.data().pos(), node));
        var newConnections = wantedConnections.stream()
                .filter(other -> other.data().graphId != nodeGraphId || !oldConnections.contains(other)).toList();
        var removedConnections = oldConnections.stream().filter(other -> !wantedConnections.contains(other)).toList();

        long mergedGraphId = nodeGraphId;
        BlockGraph mergedGraph = graph;

        for (var other : newConnections) {
            long otherGraphId = other.data().graphId;
            if (otherGraphId != mergedGraphId) {
                BlockGraph otherGraph = getGraph(otherGraphId);
                if (otherGraph == null) {
                    GraphLib.log.warn("Tried to connect to node with invalid graph id. Node: {}", other);
                    continue;
                }

                // merge the smaller graph into the larger graph
                if (otherGraph.size() > mergedGraph.size()) {
                    // merge self graph into the other graph because that would mean less node moves
                    otherGraph.merge(mergedGraph);
                    mergedGraph = otherGraph;
                    mergedGraphId = otherGraphId;
                } else {
                    mergedGraph.merge(otherGraph);
                }
            }

            mergedGraph.link(node, other);
        }

        for (var other : removedConnections) {
            mergedGraph.unlink(node, other);
        }

        if (!removedConnections.isEmpty()) {
            // Split should never leave graph empty. It also should clean up after itself.
            mergedGraph.split();
        }
    }

    private void loadGraphs(@NotNull ChunkPos pos) {
        for (int y = world.getBottomSectionCoord(); y < world.getTopSectionCoord(); y++) {
            BlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos.x, y, pos.z));
            if (chunk != null) {
                for (long id : chunk.graphsInChunk) {
                    getGraph(id);
                }
            }
        }
    }

    private void saveGraphs(@NotNull ChunkPos pos) {
        LongSet chunkSectionPillar = new LongOpenHashSet(world.getTopSectionCoord() - world.getBottomSectionCoord());
        for (int y = world.getBottomSectionCoord(); y < world.getTopSectionCoord(); y++) {
            chunkSectionPillar.add(ChunkSectionPos.asLong(pos.x, y, pos.z));
        }

        for (BlockGraph loadedGraph : loadedGraphs.values()) {
            for (long graphChunk : loadedGraph.chunks) {
                if (chunkSectionPillar.contains(graphChunk)) {
                    writeGraph(loadedGraph);
                    break;
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
            prevGraphId++;
        } while (graphExists(prevGraphId));
        markStateDirty();
        return prevGraphId;
    }

    private boolean graphExists(long id) {
        return loadedGraphs.containsKey(id) || Files.exists(getGraphFile(id));
    }

    private @NotNull Path getGraphFile(long id) {
        return graphsDir.resolve(String.format("%016X.dat", id));
    }

    private void writeGraph(@NotNull BlockGraph graph) {
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
                prevGraphId = data.getLong("prevGraphId");
            } catch (Exception e) {
                GraphLib.log.error("Error loading graph controller state file.", e);
            }
        }
    }

    private void saveState() {
        if (stateDirty) {
            NbtCompound root = new NbtCompound();

            NbtCompound data = new NbtCompound();
            data.putLong("prevGraphId", prevGraphId);

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
