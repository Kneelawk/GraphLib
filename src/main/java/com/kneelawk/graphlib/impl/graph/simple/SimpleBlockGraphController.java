package com.kneelawk.graphlib.impl.graph.simple;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.api.v1.GraphLib;
import com.kneelawk.graphlib.api.v1.GraphLibEvents;
import com.kneelawk.graphlib.api.v1.graph.BlockGraphController;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.graph.BlockNodeHolder;
import com.kneelawk.graphlib.api.v1.graph.NodeView;
import com.kneelawk.graphlib.api.v1.util.graph.Node;
import com.kneelawk.graphlib.api.v1.util.ChunkSectionUnloadTimer;
import com.kneelawk.graphlib.api.v1.util.SidedPos;
import com.kneelawk.graphlib.api.v1.world.UnloadingRegionBasedStorage;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Holds and manages all block graphs for a given world.
 * <p>
 * This is the default implementation and will likely be the only implementation. I decided to extract the intentional
 * API methods to an interface so that I could have more control over what methods were being called and to open up the
 * possibility of maybe eventually making a cubic-chunks implementation of GraphLib or something.
 */
public class SimpleBlockGraphController implements AutoCloseable, NodeView, BlockGraphController {
    /**
     * Graphs will unload 1 minute after their chunk unloads or their last use.
     */
    private static final int MAX_AGE = 20 * 60;

    final ServerWorld world;

    private final UnloadingRegionBasedStorage<SimpleBlockGraphChunk> chunks;

    private final ChunkSectionUnloadTimer timer;

    private final Path graphsDir;

    private final Path stateFile;

    private final Long2ObjectMap<SimpleBlockGraph> loadedGraphs = new Long2ObjectLinkedOpenHashMap<>();

    private final ObjectSet<BlockPos> nodeUpdates = new ObjectLinkedOpenHashSet<>();
    private final ObjectSet<UpdatePos> connectionUpdates = new ObjectLinkedOpenHashSet<>();
    private final ObjectSet<Node<BlockNodeHolder>> callbackUpdates = new ObjectLinkedOpenHashSet<>();

    private boolean stateDirty = false;
    private long prevGraphId = -1L;

    private boolean closed = false;

    public SimpleBlockGraphController(@NotNull ServerWorld world, @NotNull Path path, boolean syncChunkWrites) {
        this.chunks = new UnloadingRegionBasedStorage<>(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites,
            SimpleBlockGraphChunk::new, SimpleBlockGraphChunk::new);
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

        handleNodeUpdates();
        handleConnectionUpdates();
        handleCallbackUpdates();

        unloadGraphs();
    }

    public void saveChunk(@NotNull ChunkPos pos) {
        saveState();
        saveGraphs(pos);
        chunks.saveChunk(pos);
    }

    public void saveAll() {
        // This can be useful sometimes but causes log spam in prod
//        GLLog.info("Saving block-graph for '{}'/{}", world, world.getRegistryKey().getValue());

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
    public @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull BlockPos pos) {
        // no need for a .distict() here, because you should never have the same node be part of multiple graphs
        return getGraphsAt(pos).mapToObj(this::getGraph).filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
    }

    /**
     * Gets all nodes in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of the nodes in the given sided block-position.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull SidedPos pos) {
        return getGraphsAt(pos.pos()).mapToObj(this::getGraph).filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
    }

    /**
     * Gets the IDs of all graph with nodes in the given block-position.
     *
     * @param pos the block-position to get the IDs of graphs with nodes at.
     * @return a stream of all the IDs of graphs with nodes in the given block-position.
     */
    @Override
    public @NotNull LongStream getGraphsAt(@NotNull BlockPos pos) {
        SimpleBlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos));
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
    @Override
    public void updateNodes(@NotNull BlockPos pos) {
        nodeUpdates.add(pos.toImmutable());
    }

    /**
     * Notifies the controller that a list of block-positions have been changed and may need to have their nodes and
     * connections recalculated.
     *
     * @param poses the iterable of all the block-positions that might have been changed.
     */
    @Override
    public void updateNodes(@NotNull Iterable<BlockPos> poses) {
        for (BlockPos pos : poses) {
            // I couldn't figure out how to optimise this much, so I'm just calling onChanged for every block-pos
            updateNodes(pos);
        }
    }

    /**
     * Notifies the controller that a list of block-positions have been changed and may need to have their nodes and
     * connections recalculated.
     *
     * @param posStream the stream ob all the block-positions that might have been changed.
     */
    @Override
    public void updateNodes(@NotNull Stream<BlockPos> posStream) {
        posStream.forEach(this::updateNodes);
    }

    /**
     * Updates the connections for all the nodes at the given block-position.
     *
     * @param pos the block-position of the nodes to update connections for.
     */
    @Override
    public void updateConnections(@NotNull BlockPos pos) {
        connectionUpdates.add(new UpdateBlockPos(pos.toImmutable()));
    }

    /**
     * Updates the connections for all the nodes at the given sided block-position.
     *
     * @param pos the sided block-position of the nodes to update connections for.
     */
    @Override
    public void updateConnections(@NotNull SidedPos pos) {
        connectionUpdates.add(new UpdateSidedPos(pos));
    }

    /**
     * Gets the graph with the given ID.
     * <p>
     * Note: this <b>may</b> involve loading the graph from the filesystem.
     *
     * @param id the ID of the graph to get.
     * @return the graph with the given ID.
     */
    @Override
    @Nullable
    public SimpleBlockGraph getGraph(long id) {
        SimpleBlockGraph graph = loadedGraphs.get(id);
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
     * Gets all graph ids in the given chunk section.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @param pos the position of the chunk section to get the graphs in.
     * @return a stream of all graph ids in the given chunk section.
     */
    @Override
    public @NotNull LongStream getGraphsInChunkSection(@NotNull ChunkSectionPos pos) {
        SimpleBlockGraphChunk chunk = chunks.getIfExists(pos);
        if (chunk != null) {
            return chunk.graphsInChunk.longStream();
        } else {
            return LongStream.empty();
        }
    }

    /**
     * Gets all graph ids in the given chunk.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @param pos the position of the chunk to get the graphs in.
     * @return a stream of all graph ids in the given chunk.
     */
    @Override
    public @NotNull LongStream getGraphsInChunk(@NotNull ChunkPos pos) {
        return LongStream.range(world.getBottomSectionCoord(), world.getTopSectionCoord())
            .flatMap(y -> getGraphsInChunkSection(ChunkSectionPos.from(pos, (int) y)));
    }

    /**
     * Gets all graph ids in this graph controller.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @return a stream of all graph ids in this graph controller.
     */
    @Override
    public @NotNull LongStream getGraphs() {
        return getExistingGraphs().longStream();
    }

    /**
     * Called by the <code>/graphlib removeemptygraphs</code> command.
     * <p>
     * Removes all empty graphs. Graphs should never be empty, but it could theoretically happen if a mod isn't using
     * GraphLib correctly.
     *
     * @return the number of empty graphs removed.
     */
    @Override
    public int removeEmptyGraphs() {
        int removed = 0;

        for (long id : getExistingGraphs()) {
            if (loadedGraphs.containsKey(id)) {
                SimpleBlockGraph graph = loadedGraphs.get(id);

                if (graph.isEmpty()) {
                    GLLog.warn(
                        "Encountered empty graph! The graph's nodes probably failed to load. Removing graph... Id: {}, chunks: {}",
                        graph.getId(), graph.chunks.longStream().mapToObj(ChunkSectionPos::from).toList());

                    // must be impl because destroyGraph calls readGraph if the graph isn't already loaded
                    destroyGraphImpl(graph);

                    removed++;
                }
            } else {
                if (readGraph(id) == null) {
                    removed++;
                }
            }
        }

        return removed;
    }

    // ---- Internal Methods ---- //

    /**
     * Creates a new graph and stores it, assigning it an ID.
     *
     * @return the newly-created graph.
     */
    @NotNull SimpleBlockGraph createGraph() {
        SimpleBlockGraph graph = new SimpleBlockGraph(this, getNextGraphId());
        loadedGraphs.put(graph.getId(), graph);

        // Fire graph created event
        GraphLibEvents.GRAPH_CREATED.invoker().graphCreated(world, this, graph);

        return graph;
    }

    /**
     * Deletes a graph and all nodes it contains.
     *
     * @param id the ID of the graph to delete.
     */
    void destroyGraph(long id) {
        SimpleBlockGraph graph = getGraph(id);
        if (graph == null) {
            // The graph does not exist.
            GLLog.warn("Attempted to destroy graph that does not exist. Id: {}", id);
            return;
        }

        destroyGraphImpl(graph);

        // Fire the event
        GraphLibEvents.GRAPH_DESTROYED.invoker().graphDestroyed(world, this, id);
    }

    void addGraphInPos(long id, @NotNull BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        SimpleBlockGraphChunk chunk = chunks.getOrCreate(sectionPos);
        chunk.addGraphInPos(id, pos);

        timer.onChunkUse(sectionPos);
    }

    void removeGraphInPos(long id, @NotNull BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        SimpleBlockGraphChunk chunk = chunks.getIfExists(sectionPos);
        if (chunk != null) {
            short local = ChunkSectionPos.packLocal(pos);
            LongSet graphsInPos = chunk.graphsInPos.get(local);
            graphsInPos.remove(id);
            if (graphsInPos.isEmpty()) {
                chunk.graphsInPos.remove(local);
            }
        } else {
            GLLog.warn("Tried to remove graph from non-existent chunk. Id: {}, chunk: {}, block: {}", id, sectionPos,
                pos);
        }
    }

    void removeGraphInChunk(long id, long pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        SimpleBlockGraphChunk chunk = chunks.getIfExists(sectionPos);
        if (chunk != null) {
            chunk.graphsInChunk.remove(id);
        } else {
            GLLog.warn("Tried to remove graph fom non-existent chunk. Id: {}, chunk: {}", id, sectionPos);
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

    private void handleNodeUpdates() {
        for (BlockPos pos : nodeUpdates) {
            Set<BlockNode> nodes = GraphLib.getNodesInBlock(world, pos);
            onNodesChanged(pos, nodes);
        }
        nodeUpdates.clear();
    }

    private void handleConnectionUpdates() {
        for (UpdatePos pos : connectionUpdates) {
            if (pos instanceof UpdateBlockPos blockPos) {
                for (var node : getNodesAt(blockPos.pos).toList()) {
                    updateNodeConnections(node);
                }
            } else if (pos instanceof UpdateSidedPos sidedPos) {
                for (var node : getNodesAt(sidedPos.pos).toList()) {
                    updateNodeConnections(node);
                }
            }
        }
        connectionUpdates.clear();
    }

    void scheduleCallbackUpdate(@NotNull Node<BlockNodeHolder> node) {
        //noinspection ConstantConditions
        if (node == null) {
            GLLog.error("Something tried to schedule an update for a NULL node! This should NEVER happen.",
                new RuntimeException("Stack Trace"));
            return;
        }

        callbackUpdates.add(node);
    }

    private void handleCallbackUpdates() {
        for (var node : callbackUpdates) {
            BlockNodeHolder data = node.data();
            data.getNode().onConnectionsChanged(world, this, data.getPos(), node);
        }
        callbackUpdates.clear();
    }

    // ---- Private Methods ---- //

    private void onNodesChanged(@NotNull BlockPos pos, @NotNull Set<BlockNode> nodes) {
        Set<BlockNode> newNodes = new LinkedHashSet<>(nodes);

        for (long graphId : getGraphsAt(pos).toArray()) {
            SimpleBlockGraph graph = getGraph(graphId);
            if (graph == null) {
                GLLog.warn("Encountered invalid graph in position when detecting node changes. Id: {}, pos: {}",
                    graphId, pos);
                continue;
            }

            for (var node : graph.getNodesAt(pos).toList()) {
                BlockNode bn = node.data().getNode();
                if (!nodes.contains(bn)) {
                    graph.destroyNode(node);
                }
                newNodes.remove(bn);
            }
        }

        for (BlockNode bn : newNodes) {
            if (bn == null) {
                GLLog.warn("Something tried to add a null BlockNode! Ignoring... Pos: {}", pos,
                    new RuntimeException("Stack Trace"));
                continue;
            }

            SimpleBlockGraph newGraph = createGraph();
            Node<BlockNodeHolder> node = newGraph.createNode(pos, bn);
            updateNodeConnections(node);
        }
    }

    private void updateNodeConnections(@NotNull Node<BlockNodeHolder> node) {
        long nodeGraphId = ((SimpleBlockNodeHolder) node.data()).graphId;
        SimpleBlockGraph graph = getGraph(nodeGraphId);

        if (graph == null) {
            GLLog.warn("Tried to update node with invalid graph id. Node: {}", node);
            return;
        }

        var oldConnections = node.connections().stream().map(link -> link.other(node))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        var wantedConnections =
            new LinkedHashSet<>(node.data().getNode().findConnections(world, this, node.data().getPos(), node));
        wantedConnections.removeIf(
            other -> !other.data().getNode().canConnect(world, this, other.data().getPos(), other, node));
        var newConnections = wantedConnections.stream()
            .filter(other -> ((SimpleBlockNodeHolder) other.data()).graphId != nodeGraphId ||
                !oldConnections.contains(other)).toList();
        var removedConnections = oldConnections.stream().filter(other -> !wantedConnections.contains(other)).toList();

        long mergedGraphId = nodeGraphId;
        SimpleBlockGraph mergedGraph = graph;

        for (var other : newConnections) {
            long otherGraphId = ((SimpleBlockNodeHolder) other.data()).graphId;
            if (otherGraphId != mergedGraphId) {
                SimpleBlockGraph otherGraph = getGraph(otherGraphId);
                if (otherGraph == null) {
                    GLLog.warn("Tried to connect to node with invalid graph id. Node: {}", other);
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
        } else {
            GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(world, this, mergedGraph);
        }
    }

    private void loadGraphs(@NotNull ChunkPos pos) {
        for (int y = world.getBottomSectionCoord(); y < world.getTopSectionCoord(); y++) {
            SimpleBlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(pos.x, y, pos.z));
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

        for (SimpleBlockGraph loadedGraph : loadedGraphs.values()) {
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

            for (SimpleBlockGraph graph : loadedGraphs.values()) {
                // we want to only unload graphs that aren't in any loaded chunks
                if (graph.chunks.longStream().mapToObj(ChunkSectionPos::from).noneMatch(timer::isChunkLoaded)) {
                    toUnload.add(graph.getId());
                }
            }

            for (long id : toUnload) {
                // unload the graphs
                SimpleBlockGraph graph = loadedGraphs.remove(id);
                writeGraph(graph);
            }
        }
    }

    private void saveAllGraphs() {
        for (SimpleBlockGraph graph : loadedGraphs.values()) {
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

    private static final Pattern GRAPH_ID_PATTERN = Pattern.compile("^(?<id>[\\da-fA-F]+)\\.dat$");

    private @NotNull LongList getExistingGraphs() {
        LongList ids = new LongArrayList();
        ids.addAll(loadedGraphs.keySet());

        try (Stream<Path> children = Files.list(graphsDir)) {
            children.forEach(child -> {
                String filename = child.getFileName().toString();
                Matcher matcher = GRAPH_ID_PATTERN.matcher(filename);

                if (matcher.matches()) {
                    try {
                        long id = Long.parseLong(matcher.group("id"), 16);
                        ids.add(id);
                    } catch (NumberFormatException e) {
                        GLLog.warn("Encountered NumberFormatException while parsing graph id from filename: {}",
                            filename, e);
                    }
                } else {
                    GLLog.warn("Encountered non-graph file in graphs dir: {}", child);
                }
            });
        } catch (IOException e) {
            GLLog.error("Error listing children of {}", graphsDir, e);
        }

        return ids;
    }

    private void writeGraph(@NotNull SimpleBlockGraph graph) {
        Path graphFile = getGraphFile(graph.getId());

        NbtCompound root = new NbtCompound();
        root.put("data", graph.toTag());

        try (OutputStream os = Files.newOutputStream(graphFile)) {
            NbtIo.writeCompressed(root, os);
        } catch (IOException e) {
            GLLog.error("Unable to save graph {}.", graph.getId(), e);
        }
    }

    @Nullable
    private SimpleBlockGraph readGraph(long id) {
        Path graphFile = getGraphFile(id);

        if (!Files.exists(graphFile)) {
            return null;
        }

        try (InputStream is = Files.newInputStream(graphFile)) {
            NbtCompound root = NbtIo.readCompressed(is);
            NbtCompound data = root.getCompound("data");
            SimpleBlockGraph graph = SimpleBlockGraph.fromTag(this, id, data);
            if (graph.isEmpty()) {
                GLLog.warn(
                    "Loaded empty graph! The graph's nodes probably failed to load. Removing graph... Id: {}, chunks: {}",
                    graph.getId(), graph.chunks.longStream().mapToObj(ChunkSectionPos::from).toList());

                // must be impl because destroyGraph calls readGraph if the graph isn't already loaded
                destroyGraphImpl(graph);
                return null;
            } else {
                return graph;
            }
        } catch (IOException e) {
            GLLog.error("Unable to load graph {}. Removing graph...", id, e);

            if (Files.exists(graphFile)) {
                try {
                    Files.delete(graphFile);
                } catch (IOException ex) {
                    GLLog.error("Error deleting broken graph file: {}", graphFile, ex);
                }
            }

            return null;
        }
    }

    private void destroyGraphImpl(SimpleBlockGraph graph) {
        long id = graph.getId();

        loadedGraphs.remove(id);
        try {
            Files.deleteIfExists(getGraphFile(id));
        } catch (IOException e) {
            GLLog.error("Error removing graph file. Id: {}", id, e);
        }

        for (long sectionPos : graph.chunks) {
            SimpleBlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(sectionPos));
            if (chunk != null) {
                // Note: if this is changed to only remove from block-poses that the graph actually occupies, make sure
                // not to get those block-poses from the block-graph's graph, because the block-graph's graph will often
                // already have been cleared by the time this function is called.
                chunk.removeGraph(id);
            } else {
                GLLog.warn("Attempted to destroy graph in chunk that does not exist. Id: {}, chunk: {}", id,
                    ChunkSectionPos.from(sectionPos));
            }
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
                GLLog.error("Error loading graph controller state file.", e);
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
                    GLLog.error("Error creating graph controller state save directory.", e);
                }
            }

            try (OutputStream os = Files.newOutputStream(stateFile)) {
                NbtIo.writeCompressed(root, os);
            } catch (IOException e) {
                GLLog.error("Error saving graph controller state.", e);
                return;
            }

            stateDirty = false;
        }
    }

    private sealed interface UpdatePos {
    }

    private record UpdateBlockPos(BlockPos pos) implements UpdatePos {
    }

    private record UpdateSidedPos(SidedPos pos) implements UpdatePos {
    }
}
