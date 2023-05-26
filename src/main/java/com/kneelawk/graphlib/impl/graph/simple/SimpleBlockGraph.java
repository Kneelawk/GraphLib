package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.event.GraphLibEvents;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.NodeEntity;
import com.kneelawk.graphlib.api.node.NodeEntityDecoder;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
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
    static @NotNull SimpleBlockGraph fromTag(@NotNull SimpleGraphWorld controller, long id,
                                             @NotNull NbtCompound tag) {
        NbtList chunksTag = tag.getList("chunks", NbtElement.LONG_TYPE);
        LongSet chunks = new LongLinkedOpenHashSet();

        for (NbtElement chunkElement : chunksTag) {
            chunks.add(((NbtLong) chunkElement).longValue());
        }

        SimpleBlockGraph graph = new SimpleBlockGraph(controller, id, chunks);

        NbtList nodesTag = tag.getList("nodes", NbtElement.COMPOUND_TYPE);
        NbtList linksTag = tag.getList("links", NbtElement.COMPOUND_TYPE);

        List<@Nullable Node<SimpleNodeWrapper>> nodes = new ArrayList<>();

        for (NbtElement nodeElement : nodesTag) {
            NbtCompound com = (NbtCompound) nodeElement;
            SimpleNodeWrapper node = SimpleNodeWrapper.fromTag(controller.universe, com, id);
            if (node != null) {
                Function<NodeEntityContext, @Nullable NodeEntity> entityFactory = node.node::createNodeEntity;
                if (com.contains("entityType", NbtElement.STRING_TYPE)) {
                    Identifier entityTypeId = new Identifier(com.getString("entityType"));
                    NodeEntityDecoder decoder = controller.universe.getNodeEntityDecoder(entityTypeId);
                    if (decoder != null) {
                        entityFactory = ctx -> decoder.decode(com.get("entity"), ctx);
                    } else {
                        GLLog.warn("Encountered Node Entity with unknown type id: {}", entityTypeId);
                    }
                }

                nodes.add(graph.createNode(node.getPos(), node.getNode(), entityFactory).node);
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

    final SimpleGraphWorld controller;
    private final long id;

    private final Graph<SimpleNodeWrapper> graph = new Graph<>();
    private final Map<NodePos, NodeEntity> nodeEntities = new Object2ObjectLinkedOpenHashMap<>();
    private final Multimap<BlockPos, SimpleNodeHolder<BlockNode>> nodesInPos = LinkedHashMultimap.create();
    final LongSet chunks = new LongLinkedOpenHashSet();
    private final Map<Class<?>, List<?>> nodeTypeCache = new HashMap<>();

    public SimpleBlockGraph(@NotNull SimpleGraphWorld controller, long id) {
        this(controller, id, LongSet.of());

        // When newly-creating a graph, mark it dirty, so it'll get saved.
        // If this is a throw-away graph, it should get absorbed and deleted before the next tick.
        this.controller.markDirty(id);
    }

    private SimpleBlockGraph(@NotNull SimpleGraphWorld controller, long id, @NotNull LongSet chunks) {
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
            NbtCompound com = node.data().toTag();

            NodePos key = new NodePos(node.data().getPos(), node.data().getNode());
            NodeEntity entity = nodeEntities.get(key);
            if (entity != null) {
                com.putString("entityType", entity.getTypeId().toString());
                com.put("entity", entity.toTag());
            }

            nodesTag.add(com);
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
    public @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos) {
        return nodesInPos.get(pos).stream().map(Function.identity());
    }

    /**
     * Gets all the nodes in this graph in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given sided block-position.
     */
    @Override
    public @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos) {
        return nodesInPos.get(pos.pos()).stream()
            .filter(node -> node.getNode() instanceof SidedBlockNode sidedNode &&
                sidedNode.getSide() == pos.side()).map(node -> node.cast(SidedBlockNode.class));
    }

    @Override
    public @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos) {
        return nodeEntities.get(pos);
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
            cls -> graph.stream().filter(node -> type.isInstance(node.data().getNode()))
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
        controller.markDirty(id);
        for (var node : graph) {
            SimpleNodeWrapper data = node.data();
            data.graphId = id;
            BlockPos pos = data.getPos();
            chunks.add(ChunkSectionPos.from(pos).asLong());
            nodesInPos.put(pos, new SimpleNodeHolder<>(node));
        }
    }

    @NotNull SimpleNodeHolder<BlockNode> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node,
                                                    @NotNull Function<NodeEntityContext, @Nullable NodeEntity> entityFactory) {
        BlockPos pos = blockPos.toImmutable();
        NodePos nodePos = new NodePos(pos, node);

        SimpleNodeHolder<BlockNode> graphNode = new SimpleNodeHolder<>(graph.add(new SimpleNodeWrapper(pos, node, id)));

        NodeEntity entity = null;
        if (node.shouldHaveNodeEntity(new NodeContext(graphNode, controller.world, controller))) {
            entity = entityFactory.apply(new SimpleNodeEntityContext(graphNode, controller.world, controller));
        }
        if (entity != null) {
            nodeEntities.put(nodePos, entity);
        }

        nodesInPos.put(pos, graphNode);
        chunks.add(ChunkSectionPos.from(pos).asLong());
        nodeTypeCache.clear();
        controller.putGraphWithNode(id, nodePos);
        controller.scheduleCallbackUpdate(graphNode);
        controller.markDirty(id);

        return graphNode;
    }

    void destroyNode(@NotNull NodeHolder<BlockNode> holder) {
        // see if removing this node means removing a block-pos or a chunk
        SimpleNodeHolder<BlockNode> node = (SimpleNodeHolder<BlockNode>) holder;
        NodePos removedNode = node.toNodePos();
        BlockPos removedPos = node.getPos();
        ChunkSectionPos removedChunk = ChunkSectionPos.from(removedPos);
        nodesInPos.remove(removedPos, node);
        nodeTypeCache.clear();
        controller.markDirty(id);

        // schedule updates for each of the node's connected nodes
        for (Link<SimpleNodeWrapper> link : node.node.connections()) {
            // scheduled updates happen after, so we don't need to worry whether the node's been removed from the graph
            // yet, as it will be when these updates are actually applied
            controller.scheduleCallbackUpdate(new SimpleNodeHolder<>(link.other(node.node)));
        }
        controller.scheduleCallbackUpdate(node);

        // actually remove the node
        graph.remove(node.node);

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
        controller.removeGraphWithNode(id, removedNode);
        if (removedPos != null) {
            controller.removeGraphInPos(id, removedPos);
        }
        if (removedChunk != null) {
            long chunkLong = removedChunk.asLong();
            controller.removeGraphInChunk(id, chunkLong);
            chunks.remove(chunkLong);
        }

        // remove the associated node entity if any
        NodeEntity entity = nodeEntities.remove(new NodePos(node.getPos(), node.getNode()));
        if (entity != null) {
            entity.onDelete();
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

    void link(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b) {
        graph.link(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node);
        controller.scheduleCallbackUpdate(a);
        controller.scheduleCallbackUpdate(b);
        controller.markDirty(id);
    }

    void unlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b) {
        graph.unlink(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node);
        controller.scheduleCallbackUpdate(a);
        controller.scheduleCallbackUpdate(b);
        controller.markDirty(id);
    }

    void merge(@NotNull SimpleBlockGraph other) {
        if (other.id == id) {
            // we cannot merge with ourselves
            return;
        }

        // add our graph to all the positions and chunks the other graph is in
        for (var node : other.graph) {
            controller.putGraphWithNode(id, new NodePos(node.data().getPos(), node.data().getNode()));

            // might as well set the node's graph id here as well
            node.data().graphId = id;
        }

        graph.join(other.graph);
        nodeEntities.putAll(other.nodeEntities);
        nodesInPos.putAll(other.nodesInPos);
        chunks.addAll(other.chunks);
        nodeTypeCache.clear();
        controller.markDirty(id);

        // finally we destroy the old graph, removing it from all the graphs-in-pos and graphs-in-chunk trackers
        controller.destroyGraph(other.id);
    }

    @NotNull List<SimpleBlockGraph> split() {
        var newGraphs = graph.split();

        if (!newGraphs.isEmpty()) {
            // collect the block-nodes, block-poses, and chunks we are no longer a part of
            Set<NodePos> removedNodes = new LinkedHashSet<>();
            Set<BlockPos> removedPoses = new LinkedHashSet<>();
            LongSet removedChunks = new LongLinkedOpenHashSet();

            for (Graph<SimpleNodeWrapper> graph : newGraphs) {
                for (var node : graph) {
                    BlockPos pos = node.data().getPos();
                    removedNodes.add(new NodePos(pos, node.data().getNode()));
                    removedPoses.add(pos);
                    removedChunks.add(ChunkSectionPos.from(pos).asLong());

                    // the node is in a new graph, so it obviously isn't in our graph anymore
                    nodesInPos.remove(pos, new SimpleNodeHolder<>(node));
                }
            }

            // we aren't removing the blocks or chunks we still have
            for (var node : graph) {
                var data = node.data();
                removedPoses.remove(data.getPos());
                removedChunks.remove(ChunkSectionPos.from(data.getPos()).asLong());
            }

            // do this stuff instead of rebuilding-refs later
            controller.removeGraphInPoses(id, removedNodes, removedPoses, removedChunks);
            chunks.removeAll(removedChunks);
            nodeTypeCache.clear();
            controller.markDirty(id);

            // setup block-graphs for the newly created graphs
            List<SimpleBlockGraph> newBlockGraphs = new ArrayList<>(newGraphs.size());

            for (Graph<SimpleNodeWrapper> graph : newGraphs) {
                // create the new graph and set its nodes correctly
                SimpleBlockGraph bg = controller.createGraph();
                bg.graph.join(graph);

                // this sets the nodes' graph ids, and sets up the new block-graph's chunks and nodes-in-pos
                bg.rebuildRefs();

                for (var node : bg.graph) {
                    NodePos key = new NodePos(node.data().getPos(), node.data().getNode());

                    // Add the new graph to the graphs-in-chunks and graphs-in-poses trackers.
                    // I considered trying to group block-poses by chunk to avoid duplicate look-ups, but it didn't look
                    // like it was worth the extra computation.
                    controller.putGraphWithNode(bg.id, key);

                    // make sure to move the node entities over too
                    NodeEntity entity = nodeEntities.remove(key);
                    if (entity != null) {
                        bg.nodeEntities.put(key, entity);
                    }
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

    void onUnload() {
        for (NodeEntity entity : nodeEntities.values()) {
            entity.onUnload();
        }
    }
}
