package com.kneelawk.graphlib.graph.simple;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.kneelawk.graphlib.GLLog;
import com.kneelawk.graphlib.GraphLibEvents;
import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.SidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Graph;
import com.kneelawk.graphlib.graph.struct.Link;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

/**
 * Holds and manages a set of block nodes.
 */
public class SimpleBlockGraph implements com.kneelawk.graphlib.graph.BlockGraph {
    static @NotNull SimpleBlockGraph fromTag(@NotNull SimpleBlockGraphController controller, long id,
                                             @NotNull NbtCompound tag) {
        NbtList chunksTag = tag.getList("chunks", NbtElement.LONG_TYPE);
        LongSet chunks = new LongLinkedOpenHashSet();

        for (NbtElement chunkElement : chunksTag) {
            chunks.add(((NbtLong) chunkElement).longValue());
        }

        SimpleBlockGraph graph = new SimpleBlockGraph(controller, id, chunks);

        NbtList nodesTag = tag.getList("nodes", NbtElement.COMPOUND_TYPE);
        NbtList linksTag = tag.getList("links", NbtElement.COMPOUND_TYPE);

        List<@Nullable Node<BlockNodeHolder>> nodes = new ArrayList<>();

        for (NbtElement nodeElement : nodesTag) {
            SimpleBlockNodeHolder node = SimpleBlockNodeHolder.fromTag((NbtCompound) nodeElement, id);
            if (node != null) {
                nodes.add(graph.createNode(node.getPos(), node.getNode()));
            } else {
                // keep the gap so other nodes' links don't get messed up
                nodes.add(null);
            }
        }

        for (NbtElement linkElement : linksTag) {
            NbtCompound linkTag = (NbtCompound) linkElement;
            var first = nodes.get(linkTag.getInt("first"));
            var second = nodes.get(linkTag.getInt("second"));

            if (first != null && second != null) {
                graph.graph.link(first, second);
            }
        }

        // no need to rebuild refs as that stuff is handled by graph.createNode(...)

        return graph;
    }

    final SimpleBlockGraphController controller;
    private final long id;

    private final Graph<BlockNodeHolder> graph = new Graph<>();
    private final Multimap<BlockPos, Node<BlockNodeHolder>> nodesInPos = LinkedHashMultimap.create();
    final LongSet chunks = new LongLinkedOpenHashSet();

    public SimpleBlockGraph(@NotNull SimpleBlockGraphController controller, long id) {
        this(controller, id, LongSet.of());
    }

    private SimpleBlockGraph(@NotNull SimpleBlockGraphController controller, long id, @NotNull LongSet chunks) {
        this.controller = controller;
        this.id = id;
        this.chunks.addAll(chunks);
    }

    @NotNull NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        NbtList chunksTag = new NbtList();

        for (long chunk : chunks) {
            chunksTag.add(NbtLong.of(chunk));
        }

        tag.put("chunks", chunksTag);

        var nodes = graph.stream().toList();
        var nodeIndexMap = IntStream.range(0, nodes.size()).mapToObj(i -> new Pair<>(nodes.get(i), i))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        NbtList nodesTag = new NbtList();

        for (var node : nodes) {
            nodesTag.add(((SimpleBlockNodeHolder) node.data()).toTag());
        }

        tag.put("nodes", nodesTag);

        NbtList linksTag = new NbtList();

        for (var link : nodes.stream().flatMap(node -> node.connections().stream()).distinct().toList()) {
            if (!nodeIndexMap.containsKey(link.first())) {
                GLLog.warn(
                        "Attempted to save link with non-existent node. Graph Id: {}, offending node: {}, missing node: {}",
                        id, link.second(), link.first());
                continue;
            }
            if (!nodeIndexMap.containsKey(link.second())) {
                GLLog.warn(
                        "Attempted to save link with non-existent node. Graph Id: {}, offending node: {}, missing node: {}",
                        id, link.first(), link.second());
                continue;
            }

            NbtCompound linkTag = new NbtCompound();
            linkTag.putInt("first", nodeIndexMap.get(link.first()));
            linkTag.putInt("second", nodeIndexMap.get(link.second()));
            linksTag.add(linkTag);
        }

        tag.put("links", linksTag);

        return tag;
    }

    /**
     * Gets this graph's graph ID.
     *
     * @return the ID of this graph.
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Gets all the nodes in this graph in the given block-position.
     *
     * @param pos the block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given block-position.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull BlockPos pos) {
        return nodesInPos.get(pos).stream();
    }

    /**
     * Gets all the nodes in this graph in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given sided block-position.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull SidedPos pos) {
        return nodesInPos.get(pos.pos()).stream()
                .filter(node -> node.data().getNode() instanceof SidedBlockNode sidedNode &&
                        sidedNode.getSide() == pos.side());
    }

    /**
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @Override
    public @NotNull Stream<Node<BlockNodeHolder>> getNodes() {
        return graph.stream();
    }

    /**
     * Gets all the chunk sections that this graph currently has nodes in.
     *
     * @return a stream of all the chunk sections this graph is in.
     */
    @Override
    public @NotNull Stream<ChunkSectionPos> getChunks() {
        return chunks.longStream().mapToObj(ChunkSectionPos::from);
    }

    /**
     * Gets the number of nodes in this graph.
     *
     * @return the number of nodes in this graph.
     */
    @Override
    public int size() {
        return graph.size();
    }

    /**
     * Gets whether this graph is empty.
     *
     * @return <code>true</code> if this graph has no nodes, <code>false</code> otherwise.
     */
    @Override
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    private void rebuildRefs() {
        // Ok, we did end up needing this "rebuildRefs" after all, but only under specific circumstances
        chunks.clear();
        nodesInPos.clear();
        for (var node : graph) {
            SimpleBlockNodeHolder data = (SimpleBlockNodeHolder) node.data();
            data.graphId = id;
            BlockPos pos = data.getPos();
            chunks.add(ChunkSectionPos.from(pos).asLong());
            nodesInPos.put(pos, node);
        }
    }

    @NotNull Node<BlockNodeHolder> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node) {
        BlockPos pos = blockPos.toImmutable();

        Node<BlockNodeHolder> graphNode = graph.add(new SimpleBlockNodeHolder(pos, node, id));
        nodesInPos.put(pos, graphNode);
        chunks.add(ChunkSectionPos.from(pos).asLong());
        controller.addGraphInPos(id, pos);
        controller.scheduleCallbackUpdate(graphNode);
        return graphNode;
    }

    void destroyNode(@NotNull Node<BlockNodeHolder> node) {
        // see if removing this node means removing a block-pos or a chunk
        BlockPos removedPos = node.data().getPos();
        ChunkSectionPos removedChunk = ChunkSectionPos.from(removedPos);
        nodesInPos.remove(removedPos, node);

        // schedule updates for each of the node's connected nodes
        for (Link<BlockNodeHolder> link : node.connections()) {
            // scheduled updates happen after, so we don't need to worry whether the node's been removed from the graph
            // yet, as it will be when these updates are actually applied
            controller.scheduleCallbackUpdate(link.other(node));
        }
        controller.scheduleCallbackUpdate(node);

        // actually remove the node
        graph.remove(node);

        // check to see if the pos or chunk are used by any of our other nodes
        for (var ourNode : graph) {
            BlockPos pos = ourNode.data().getPos();
            if (pos.equals(removedPos)) {
                removedPos = null;
                removedChunk = null;
                break;
            }
            if (ChunkSectionPos.from(pos).equals(removedChunk)) {
                removedChunk = null;
            }
        }

        // do the house-cleaning
        if (removedPos != null) {
            controller.removeGraphInPos(id, removedPos);
        }
        if (removedChunk != null) {
            long chunkLong = removedChunk.asLong();
            controller.removeGraphInChunk(id, chunkLong);
            chunks.remove(chunkLong);
        }

        if (graph.isEmpty()) {
            // This only happens if this graph contained a single node before and that node has now been removed.
            controller.destroyGraph(id);
        } else {
            // Split leaves both new graphs and this graph in valid states as far as refs go.
            // Also, split is guaranteed not to leave this graph empty.
            split();
        }
    }

    void link(@NotNull Node<BlockNodeHolder> a, @NotNull Node<BlockNodeHolder> b) {
        graph.link(a, b);
        controller.scheduleCallbackUpdate(a);
        controller.scheduleCallbackUpdate(b);
    }

    void unlink(@NotNull Node<BlockNodeHolder> a, @NotNull Node<BlockNodeHolder> b) {
        graph.unlink(a, b);
        controller.scheduleCallbackUpdate(a);
        controller.scheduleCallbackUpdate(b);
    }

    void merge(@NotNull SimpleBlockGraph other) {
        if (other.id == id) {
            // we cannot merge with ourselves
            return;
        }

        // add our graph to all the positions and chunks the other graph is in
        for (var node : other.graph) {
            controller.addGraphInPos(id, node.data().getPos());

            // might as well set the node's graph id here as well
            ((SimpleBlockNodeHolder) node.data()).graphId = id;
        }

        graph.join(other.graph);
        nodesInPos.putAll(other.nodesInPos);
        chunks.addAll(other.chunks);

        // finally we destroy the old graph, removing it from all the graphs-in-pos and graphs-in-chunk trackers
        controller.destroyGraph(other.id);
    }

    @NotNull List<SimpleBlockGraph> split() {
        var newGraphs = graph.split();

        if (!newGraphs.isEmpty()) {
            // collect the block-poses and chunks we are no longer a part of
            Set<BlockPos> removedPoses = new LinkedHashSet<>();
            LongSet removedChunks = new LongLinkedOpenHashSet();

            for (Graph<BlockNodeHolder> graph : newGraphs) {
                for (var node : graph) {
                    BlockPos pos = node.data().getPos();
                    removedPoses.add(pos);
                    removedChunks.add(ChunkSectionPos.from(pos).asLong());

                    // the node is in a new graph, so it obviously isn't in our graph anymore
                    nodesInPos.remove(pos, node);
                }
            }

            // we aren't removing the blocks or chunks we still have
            for (var node : graph) {
                var data = node.data();
                removedPoses.remove(data.getPos());
                removedChunks.remove(ChunkSectionPos.from(data.getPos()).asLong());
            }

            // do this stuff instead of rebuilding-refs later
            controller.removeGraphInPoses(id, removedPoses, removedChunks);
            chunks.removeAll(removedChunks);

            // setup block-graphs for the newly created graphs
            List<SimpleBlockGraph> newBlockGraphs = new ArrayList<>(newGraphs.size());

            for (Graph<BlockNodeHolder> graph : newGraphs) {
                // create the new graph and set its nodes correctly
                SimpleBlockGraph bg = controller.createGraph();
                bg.graph.join(graph);

                // this sets the nodes' graph ids, and sets up the new block-graph's chunks and nodes-in-pos
                bg.rebuildRefs();

                // add the new graph to the graphs-in-chunks and graphs-in-poses trackers
                for (var node : bg.graph) {
                    // I considered trying to group block-poses by chunk to avoid duplicate look-ups, but it didn't look
                    // like it was worth the extra computation.
                    controller.addGraphInPos(bg.id, node.data().getPos());
                }

                newBlockGraphs.add(bg);

                // Fire update events for the new graphs
                GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(controller.world, controller, bg);
            }

            // Fire the update events
            GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(controller.world, controller, this);

            return newBlockGraphs;
        } else {
            // Fire the update events
            GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(controller.world, controller, this);

            return List.of();
        }
    }
}
