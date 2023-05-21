package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.event.GraphLibEvents;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.NodeKey;
import com.kneelawk.graphlib.api.node.PosLinkKey;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Graph;
import com.kneelawk.graphlib.api.util.graph.Link;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.GLLog;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

/**
 * Holds and manages a set of block nodes.
 */
public class SimpleBlockGraph implements BlockGraph {
    static @NotNull SimpleBlockGraph fromTag(@NotNull SimpleGraphWorld world, long id, @NotNull NbtCompound tag) {
        NbtList chunksTag = tag.getList("chunks", NbtElement.LONG_TYPE);
        LongSet chunks = new LongLinkedOpenHashSet();

        for (NbtElement chunkElement : chunksTag) {
            chunks.add(((NbtLong) chunkElement).longValue());
        }

        SimpleBlockGraph graph = new SimpleBlockGraph(world, id, chunks);

        NbtList nodesTag = tag.getList("nodes", NbtElement.COMPOUND_TYPE);
        NbtList linksTag = tag.getList("links", NbtElement.COMPOUND_TYPE);

        List<@Nullable PosNodeKey> nodes = new ArrayList<>();

        for (NbtElement nodeElement : nodesTag) {
            SimpleNodeCodec node =
                SimpleNodeCodec.decode(id, world.universe, world.world, world, (NbtCompound) nodeElement);
            if (node != null) {
                nodes.add(graph.createNode(node.pos(), node.node(), node.ctx()).toNodeKey());
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

    final SimpleGraphWorld world;
    private final long id;

    private final Graph<PosNodeKey, SimpleNodeWrapper> graph = new Graph<>();
    private final Table<BlockPos, NodeKey, SimpleNodeHolder<BlockNode>> nodesInPos = HashBasedTable.create();
    final LongSet chunks = new LongLinkedOpenHashSet();
    private final Map<Class<?>, List<?>> nodeTypeCache = new HashMap<>();

    public SimpleBlockGraph(@NotNull SimpleGraphWorld world, long id) {
        this(world, id, LongSet.of());

        // When newly-creating a graph, mark it dirty, so it'll get saved.
        // If this is a throw-away graph, it should get absorbed and deleted before the next tick.
        this.world.markDirty(id);
    }

    private SimpleBlockGraph(@NotNull SimpleGraphWorld world, long id, @NotNull LongSet chunks) {
        this.world = world;
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

        List<Node<PosNodeKey, SimpleNodeWrapper>> nodes = new ObjectArrayList<>(graph.values());
        int nodeCount = nodes.size();

        Object2IntMap<PosNodeKey> nodeIndexMap = new Object2IntOpenHashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            nodeIndexMap.put(nodes.get(i).key(), i);
        }

        NbtList nodesTag = new NbtList();

        for (Node<PosNodeKey, SimpleNodeWrapper> node : nodes) {
            nodesTag.add(SimpleNodeCodec.encode(node.key().pos(), node.value().node));
        }

        tag.put("nodes", nodesTag);

        NbtList linksTag = new NbtList();

        Set<PosLinkKey> connections = new ObjectLinkedOpenHashSet<>();
        for (Node<PosNodeKey, SimpleNodeWrapper> node : nodes) {
            for (Link<PosNodeKey, SimpleNodeWrapper> link : node.connections().values()) {
                connections.add(PosLinkKey.from(link));
            }
        }

        for (PosLinkKey link : connections) {
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
            linkTag.putInt("first", nodeIndexMap.getInt(link.first()));
            linkTag.putInt("second", nodeIndexMap.getInt(link.second()));
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
    public @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos) {
        return nodesInPos.row(pos).values().stream().map(Function.identity());
    }

    /**
     * Gets all the nodes in this graph in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given sided block-position.
     */
    @Override
    public @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos) {
        return nodesInPos.row(pos.pos()).values().stream()
            .filter(node -> node.getNode() instanceof SidedBlockNode sidedNode && sidedNode.getSide() == pos.side())
            .map(node -> node.cast(SidedBlockNode.class));
    }

    /**
     * Gets the node with the given key, if it exists.
     *
     * @param key the key to look for the node by.
     * @return a node holder holding the node with the given key.
     */
    @Override
    public @Nullable NodeHolder<BlockNode> getNode(PosNodeKey key) {
        return new SimpleNodeHolder<>(graph.get(key));
    }

    /**
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodes() {
        return graph.stream().map(SimpleNodeHolder::new);
    }

    /**
     * Gets all nodes in this graph with the given type.
     *
     * @param type the type that all returned nodes must be.
     * @return a stream of all the nodes in this graph with the given type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockNode> @NotNull Stream<NodeHolder<T>> getNodesOfType(@NotNull Class<T> type) {
        List<NodeHolder<T>> nodesOfType = (List<NodeHolder<T>>) nodeTypeCache.computeIfAbsent(type,
            cls -> graph.stream().filter(node -> type.isInstance(node.value().getNode()))
                .<NodeHolder<T>>map(SimpleNodeHolder::new).toList());
        return nodesOfType.stream();
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
        nodeTypeCache.clear();
        world.markDirty(id);
        for (var node : graph) {
            SimpleNodeWrapper data = node.value();
            data.graphId = id;
            BlockPos pos = node.key().pos();
            chunks.add(ChunkSectionPos.from(pos).asLong());
            SimpleNodeHolder<BlockNode> holder = new SimpleNodeHolder<>(node);
            nodesInPos.put(pos, holder.getNode().getKey(), holder);
        }
    }

    @NotNull SimpleNodeHolder<BlockNode> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node,
                                                    @NotNull SimpleNodeContext ctx) {
        BlockPos pos = blockPos.toImmutable();
        PosNodeKey key = new PosNodeKey(pos, node.getKey());

        SimpleNodeHolder<BlockNode> graphNode = new SimpleNodeHolder<>(
            graph.add(key, new SimpleNodeWrapper(node, id)));
        ctx.setSelf(graphNode);
        node.onInit();

        nodesInPos.put(pos, graphNode.getNode().getKey(), graphNode);
        chunks.add(ChunkSectionPos.from(pos).asLong());
        nodeTypeCache.clear();
        world.putGraphWithKey(id, key);
        world.scheduleCallbackUpdate(graphNode);
        world.markDirty(id);
        return graphNode;
    }

    void destroyNode(@NotNull NodeHolder<BlockNode> holder) {
        // see if removing this node means removing a block-pos or a chunk
        SimpleNodeHolder<BlockNode> node = (SimpleNodeHolder<BlockNode>) holder;
        PosNodeKey removedKey = node.toNodeKey();
        BlockPos removedPos = node.getPos();
        ChunkSectionPos removedChunk = ChunkSectionPos.from(removedPos);
        nodesInPos.remove(removedPos, node.getNode().getKey());
        nodeTypeCache.clear();
        world.markDirty(id);

        // schedule updates for each of the node's connected nodes
        for (NodeLink link : node.getConnections().values()) {
            // scheduled updates happen after, so we don't need to worry whether the node's been removed from the graph
            // yet, as it will be when these updates are actually applied
            world.scheduleCallbackUpdate(link.other(node.toNodeKey()));
        }
        world.scheduleCallbackUpdate(node);

        // actually remove the node
        graph.remove(node.toNodeKey());

        // check to see if the pos or chunk are used by any of our other nodes
        for (var ourNode : graph) {
            BlockPos pos = ourNode.key().pos();
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
        world.removeGraphWithKey(id, removedKey);
        if (removedPos != null) {
            world.removeGraphInPos(id, removedPos);
        }
        if (removedChunk != null) {
            long chunkLong = removedChunk.asLong();
            world.removeGraphInChunk(id, chunkLong);
            chunks.remove(chunkLong);
        }

        // notify the node it's been removed
        node.getNode().onDelete();

        if (graph.isEmpty()) {
            // This only happens if this graph contained a single node before and that node has now been removed.
            world.destroyGraph(id);
        } else {
            // Split leaves both new graphs and this graph in valid states as far as refs go.
            // Also, split is guaranteed not to leave this graph empty.
            split();
        }
    }

    void link(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b) {
        graph.link(a.toNodeKey(), b.toNodeKey());
        world.scheduleCallbackUpdate(a);
        world.scheduleCallbackUpdate(b);
        world.markDirty(id);
    }

    void unlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b) {
        graph.unlink(a.toNodeKey(), b.toNodeKey());
        world.scheduleCallbackUpdate(a);
        world.scheduleCallbackUpdate(b);
        world.markDirty(id);
    }

    void merge(@NotNull SimpleBlockGraph other) {
        if (other.id == id) {
            // we cannot merge with ourselves
            return;
        }

        // add our graph to all the positions and chunks the other graph is in
        for (var node : other.graph) {
            world.putGraphWithKey(id, node.key());

            // might as well set the node's graph id here as well
            node.value().graphId = id;
        }

        graph.join(other.graph);
        nodesInPos.putAll(other.nodesInPos);
        chunks.addAll(other.chunks);
        nodeTypeCache.clear();
        world.markDirty(id);

        // finally we destroy the old graph, removing it from all the graphs-in-pos and graphs-in-chunk trackers
        world.destroyGraph(other.id);
    }

    @NotNull List<SimpleBlockGraph> split() {
        var newGraphs = graph.split();

        if (!newGraphs.isEmpty()) {
            // collect the keys, block-poses, and chunks we are no longer a part of
            Set<PosNodeKey> removedKeys = new LinkedHashSet<>();
            Set<BlockPos> removedPoses = new LinkedHashSet<>();
            LongSet removedChunks = new LongLinkedOpenHashSet();

            for (Graph<PosNodeKey, SimpleNodeWrapper> graph : newGraphs) {
                for (var node : graph) {
                    removedKeys.add(node.key());
                    BlockPos pos = node.key().pos();
                    removedPoses.add(pos);
                    removedChunks.add(ChunkSectionPos.from(pos).asLong());

                    // the node is in a new graph, so it obviously isn't in our graph anymore
                    nodesInPos.remove(pos, node.key().nodeKey());
                }
            }

            // we aren't removing the blocks or chunks we still have
            for (var node : graph) {
                var data = node.key();
                removedPoses.remove(data.pos());
                removedChunks.remove(ChunkSectionPos.from(data.pos()).asLong());
            }

            // do this stuff instead of rebuilding-refs later
            world.removeGraphInPoses(id, removedKeys, removedPoses, removedChunks);
            chunks.removeAll(removedChunks);
            nodeTypeCache.clear();
            world.markDirty(id);

            // setup block-graphs for the newly created graphs
            List<SimpleBlockGraph> newBlockGraphs = new ArrayList<>(newGraphs.size());

            for (Graph<PosNodeKey, SimpleNodeWrapper> graph : newGraphs) {
                // create the new graph and set its nodes correctly
                SimpleBlockGraph bg = world.createGraph();
                bg.graph.join(graph);

                // this sets the nodes' graph ids, and sets up the new block-graph's chunks and nodes-in-pos
                bg.rebuildRefs();

                // add the new graph to the graphs-in-chunks and graphs-in-poses trackers
                for (var node : bg.graph) {
                    // I considered trying to group block-poses by chunk to avoid duplicate look-ups, but it didn't look
                    // like it was worth the uniqueData computation.
                    world.putGraphWithKey(bg.id, node.key());
                }

                newBlockGraphs.add(bg);

                // Fire update events for the new graphs
                GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(world.world, world, bg);
            }

            // Fire the update events
            GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(world.world, world, this);

            return newBlockGraphs;
        } else {
            // Fire the update events
            GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(world.world, world, this);

            return List.of();
        }
    }

    void onUnload() {
        for (var node : graph) {
            node.value().getNode().onUnload();
        }
    }
}
