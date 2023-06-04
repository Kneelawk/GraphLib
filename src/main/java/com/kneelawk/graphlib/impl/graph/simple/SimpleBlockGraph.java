package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkEntityFactory;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntityFactory;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
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
        NbtCompound graphEntities = tag.getCompound("graphEntities");

        List<@Nullable NodeHolder<BlockNode>> nodes = new ArrayList<>();

        for (NbtElement nodeElement : nodesTag) {
            NbtCompound com = (NbtCompound) nodeElement;
            SimpleNodeWrapper node = SimpleNodeWrapper.fromTag(controller.universe, com, id);
            if (node != null) {
                NodeEntityFactory entityFactory = node.node::createNodeEntity;
                if (com.contains("entityType", NbtElement.STRING_TYPE)) {
                    Identifier entityTypeId = new Identifier(com.getString("entityType"));
                    NodeEntityDecoder decoder = controller.universe.getNodeEntityDecoder(entityTypeId);
                    if (decoder != null) {
                        entityFactory = ctx -> decoder.decode(com.get("entity"), ctx);
                    } else {
                        GLLog.warn("Encountered Node Entity with unknown type id: {}", entityTypeId);
                    }
                }

                nodes.add(graph.createNode(node.getPos(), node.getNode(), entityFactory));
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
                LinkKey key = EmptyLinkKey.INSTANCE;
                if (linkTag.contains("keyType", NbtElement.STRING_TYPE)) {
                    Identifier keyTypeId = new Identifier(linkTag.getString("keyType"));
                    LinkKeyDecoder decoder = controller.universe.getLinkKeyDecoder(keyTypeId);
                    if (decoder != null) {
                        LinkKey decodedKey = decoder.decode(linkTag.get("key"));
                        if (decodedKey != null) {
                            key = decodedKey;
                        }
                    } else {
                        GLLog.warn("Encountered link key with unknown type id: {}", keyTypeId);
                    }
                }

                LinkEntityFactory entityFactory = key::createLinkEntity;
                if (linkTag.contains("entityType", NbtElement.STRING_TYPE)) {
                    Identifier entityTypeId = new Identifier(linkTag.getString("entityType"));
                    LinkEntityDecoder decoder = controller.universe.getLinkEntityDecoder(entityTypeId);
                    if (decoder != null) {
                        entityFactory = ctx -> decoder.decode(linkTag.get("entity"), ctx);
                    } else {
                        GLLog.warn("Encountered Link Entity with unknown id: {}", entityTypeId);
                    }
                }

                graph.link(first, second, key, entityFactory);
            }
        }

        // decode the graph entities
        for (GraphEntityType<?> type : controller.universe.getAllGraphEntityTypes()) {
            SimpleGraphEntityContext ctx = new SimpleGraphEntityContext(controller.world, controller, graph);
            if (graphEntities.contains(type.id().toString(), NbtElement.COMPOUND_TYPE)) {
                NbtCompound entityCom = graphEntities.getCompound(type.id().toString());
                GraphEntity<?> entity = type.decoder().decode(entityCom.get("entity"), ctx);
                if (entity == null) {
                    entity = type.factory().createNew(ctx);
                }
                graph.graphEntities.put(type, entity);
            } else {
                GLLog.warn("Graph missing graph entity of type: {}, creating a new one...", type.id());
                GraphEntity<?> entity = type.factory().createNew(ctx);
                graph.graphEntities.put(type, entity);
            }
        }

        // no need to rebuild refs as that stuff is handled by graph.createNode(...)

        return graph;
    }

    final SimpleGraphWorld world;
    private final long id;

    private final Graph<SimpleNodeWrapper, LinkKey> graph = new Graph<>();
    private final Map<NodePos, NodeEntity> nodeEntities = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<LinkPos, LinkEntity> linkEntities = new Object2ObjectLinkedOpenHashMap<>();
    private final Multimap<BlockPos, NodeHolder<BlockNode>> nodesInPos = LinkedHashMultimap.create();
    private final Map<NodePos, NodeHolder<BlockNode>> nodesToHolders = new Object2ObjectLinkedOpenHashMap<>();
    final LongSet chunks = new LongLinkedOpenHashSet();
    private final Map<Class<?>, List<?>> nodeTypeCache = new HashMap<>();
    private final Map<GraphEntityType<?>, GraphEntity<?>> graphEntities = new Object2ObjectLinkedOpenHashMap<>();

    public SimpleBlockGraph(@NotNull SimpleGraphWorld world, long id, boolean initializeGraphEntities) {
        this(world, id, LongSet.of());

        // When newly-creating a graph, mark it dirty, so it'll get saved.
        // If this is a throw-away graph, it should get absorbed and deleted before the next tick.
        this.world.markDirty(id);

        // Add all the empty graph entities
        if (initializeGraphEntities) {
            for (GraphEntityType<?> type : this.world.universe.getAllGraphEntityTypes()) {
                graphEntities.put(type, type.factory()
                    .createNew(new SimpleGraphEntityContext(this.world.world, this.world, this)));
            }
        }
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
                NbtElement entityTag = entity.toTag();
                if (entityTag != null) {
                    com.put("entity", entityTag);
                }
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

            LinkKey key = link.key();
            linkTag.putString("keyType", key.getTypeId().toString());
            NbtElement keyTag = key.toTag();
            if (keyTag != null) {
                linkTag.put("key", keyTag);
            }

            LinkEntity entity = linkEntities.get(
                new LinkPos(link.first().data().getPos(), link.first().data().getNode(), link.second().data().getPos(),
                    link.second().data().getNode(), link.key()));
            if (entity != null) {
                linkTag.putString("entityType", entity.getTypeId().toString());
                NbtElement entityTag = entity.toTag();
                if (entityTag != null) {
                    linkTag.put("entity", entityTag);
                }
            }

            linksTag.add(linkTag);
        }

        tag.put("links", linksTag);

        NbtCompound graphEntitiesCom = new NbtCompound();

        for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
            NbtCompound graphEntityCom = new NbtCompound();

            NbtElement entityTag = entry.getValue().toTag();
            if (entityTag != null) {
                graphEntityCom.put("entity", entityTag);
            }

            graphEntitiesCom.put(entry.getKey().id().toString(), graphEntityCom);
        }

        tag.put("graphEntities", graphEntitiesCom);

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
        return nodesInPos.get(pos).stream();
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

    /**
     * Checks whether the given node actually exists in this graph.
     *
     * @param pos the position of the node to check.
     * @return <code>true</code> if the given node actually exists.
     */
    @Override
    public boolean nodeExistsAt(@NotNull NodePos pos) {
        return nodesToHolders.containsKey(pos);
    }

    /**
     * Gets the node holder at a specific position.
     *
     * @param pos the position of the node to get.
     * @return the node holder at the given position.
     */
    @Override
    public @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos) {
        return nodesToHolders.get(pos);
    }

    /**
     * Checks whether the given link actually exists in this graph.
     *
     * @param pos the position of the link to check.
     * @return <code>true</code> if the given link actually exists.
     */
    @Override
    public boolean linkExistsAt(@NotNull LinkPos pos) {
        SimpleNodeHolder<BlockNode> node1 = (SimpleNodeHolder<BlockNode>) nodesToHolders.get(pos.first());
        SimpleNodeHolder<BlockNode> node2 = (SimpleNodeHolder<BlockNode>) nodesToHolders.get(pos.second());

        if (node1 == null || node2 == null) return false;

        Link<SimpleNodeWrapper, LinkKey> rawLink = new Link<>(node1.node, node2.node, pos.key());

        return node1.node.connections().contains(rawLink) && node2.node.connections().contains(rawLink);
    }

    /**
     * Gets the link holder at the given position, if it exists.
     *
     * @param pos the position to get the link at.
     * @return the link holder at the given position, if it exists.
     */
    @Override
    public @Nullable LinkHolder<LinkKey> getLinkAt(@NotNull LinkPos pos) {
        SimpleNodeHolder<BlockNode> node1 = (SimpleNodeHolder<BlockNode>) nodesToHolders.get(pos.first());
        SimpleNodeHolder<BlockNode> node2 = (SimpleNodeHolder<BlockNode>) nodesToHolders.get(pos.second());

        if (node1 == null || node2 == null) return null;

        Link<SimpleNodeWrapper, LinkKey> rawLink = new Link<>(node1.node, node2.node, pos.key());

        if (!node1.node.connections().contains(rawLink) || !node2.node.connections().contains(rawLink)) return null;

        return new SimpleLinkHolder<>(world.world, world, rawLink);
    }

    @Override
    public @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos) {
        return nodeEntities.get(pos);
    }

    @Override
    public @Nullable LinkEntity getLinkEntity(@NotNull LinkPos pos) {
        return linkEntities.get(pos);
    }

    /**
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodes() {
        return graph.stream().map(node -> new SimpleNodeHolder<>(world.world, world, node));
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
                .<NodeHolder<T>>map(node1 -> new SimpleNodeHolder<>(world.world, world, node1)).toList());
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
     * Gets a graph entity attached to this graph.
     *
     * @param type the type of graph entity to retrieve.
     * @return the given graph entity attached to this graph.
     * @throws IllegalArgumentException if the given graph entity type has not been registered with this graph's universe.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <G extends GraphEntity<G>> @NotNull G getGraphEntity(GraphEntityType<G> type) {
        GraphEntity<?> entity = graphEntities.get(type);
        if (entity == null) throw new IllegalArgumentException("No graph entity type registered with id: " + type.id());
        return (G) entity;
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
        nodesToHolders.clear();
        nodeTypeCache.clear();
        world.markDirty(id);
        for (var node : graph) {
            SimpleNodeWrapper data = node.data();
            data.graphId = id;
            BlockPos pos = data.getPos();
            chunks.add(ChunkSectionPos.from(pos).asLong());
            NodeHolder<BlockNode> holder = new SimpleNodeHolder<>(world.world, world, node);
            nodesInPos.put(pos, holder);
            nodesToHolders.put(holder.toNodePos(), holder);
        }
    }

    @NotNull SimpleNodeHolder<BlockNode> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node,
                                                    @NotNull NodeEntityFactory entityFactory) {
        BlockPos pos = blockPos.toImmutable();
        NodePos nodePos = new NodePos(pos, node);

        SimpleNodeHolder<BlockNode> graphNode = new SimpleNodeHolder<>(world.world, world,
            graph.add(new SimpleNodeWrapper(pos, node, id)));

        NodeEntity nodeEntity;
        if (!nodeEntities.containsKey(nodePos)) {
            NodeEntity newNodeEntity = null;
            if (node.shouldHaveNodeEntity(graphNode)) {
                newNodeEntity =
                    entityFactory.createNew(new SimpleNodeEntityContext(graphNode, world.world, world));
            }
            if (newNodeEntity != null) {
                nodeEntities.put(nodePos, newNodeEntity);
            }
            nodeEntity = newNodeEntity;
        } else {
            nodeEntity = nodeEntities.get(nodePos);
        }

        nodesInPos.put(pos, graphNode);
        nodesToHolders.put(nodePos, graphNode);
        chunks.add(ChunkSectionPos.from(pos).asLong());
        nodeTypeCache.clear();
        world.putGraphWithNode(id, nodePos);
        world.scheduleCallbackUpdate(graphNode);

        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onNodeCreated(graphNode, nodeEntity);
        }

        world.markDirty(id);

        return graphNode;
    }

    void destroyNode(@NotNull NodeHolder<BlockNode> holder) {
        // see if removing this node means removing a block-pos or a chunk
        SimpleNodeHolder<BlockNode> node = (SimpleNodeHolder<BlockNode>) holder;
        NodePos removedNode = node.toNodePos();
        BlockPos removedPos = node.getPos();
        ChunkSectionPos removedChunk = ChunkSectionPos.from(removedPos);
        nodesInPos.remove(removedPos, node);
        nodesToHolders.remove(removedNode);
        nodeTypeCache.clear();
        world.markDirty(id);

        Map<LinkPos, LinkEntity> removedLinks = new Object2ObjectLinkedOpenHashMap<>();

        // schedule updates for each of the node's connected nodes while collecting removed connections
        for (Link<SimpleNodeWrapper, LinkKey> link : node.node.connections()) {
            // scheduled updates happen after, so we don't need to worry whether the node's been removed from the graph
            // yet, as it will be when these updates are actually applied
            world.scheduleCallbackUpdate(
                new SimpleNodeHolder<>(world.world, world, link.other(node.node)));

            // collect the link entities to be removed
            LinkPos linkKey =
                new LinkPos(link.first().data().getPos(), link.first().data().getNode(), link.second().data().getPos(),
                    link.second().data().getNode(), link.key());
            LinkEntity linkEntity = linkEntities.get(linkKey);
            if (linkEntity != null) {
                removedLinks.put(linkKey, linkEntity);
            }
        }
        world.scheduleCallbackUpdate(node);

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
        world.removeGraphWithNode(id, removedNode);
        if (removedPos != null) {
            world.removeGraphInPos(id, removedPos);
        }
        if (removedChunk != null) {
            long chunkLong = removedChunk.asLong();
            world.removeGraphInChunk(id, chunkLong);
            chunks.remove(chunkLong);
        }

        // remove the associated node entity if any
        NodeEntity nodeEntity = nodeEntities.remove(node.toNodePos());
        if (nodeEntity != null) {
            nodeEntity.onDelete();
        }

        // remove connected link entities
        for (Map.Entry<LinkPos, LinkEntity> entry : removedLinks.entrySet()) {
            linkEntities.remove(entry.getKey());
            entry.getValue().onDelete();
        }

        // notify the graph entities that a node was destroyed
        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onNodeDestroyed(holder, nodeEntity, removedLinks);
        }

        if (graph.isEmpty()) {
            // This only happens if this graph contained a single node before and that node has now been removed.
            world.destroyGraph(id);
        } else {
            // Split leaves both new graphs and this graph in valid states as far as refs go.
            // Also, split is guaranteed not to leave this graph empty.
            split();
        }
    }

    @NotNull LinkHolder<LinkKey> link(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key,
                                      @NotNull LinkEntityFactory entityFactory) {
        LinkHolder<LinkKey> link = new SimpleLinkHolder<>(world.world, world,
            graph.link(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node, key));
        LinkPos linkPos = link.toLinkPos();

        LinkEntity linkEntity;
        if (!linkEntities.containsKey(linkPos)) {
            LinkEntity newLinkEntity = null;
            if (key.shouldHaveLinkEntity(link)) {
                newLinkEntity =
                    entityFactory.createNew(new SimpleLinkEntityContext(link, world.world, world));
            }
            if (newLinkEntity != null) {
                linkEntities.put(linkPos, newLinkEntity);
            }
            linkEntity = newLinkEntity;
        } else {
            linkEntity = linkEntities.get(linkPos);
        }

        world.scheduleCallbackUpdate(a);
        world.scheduleCallbackUpdate(b);

        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onLink(a, b, linkEntity);
        }

        world.markDirty(id);

        return link;
    }

    boolean unlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key) {
        boolean linkRemoved =
            graph.unlink(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node, key);

        LinkEntity entity = linkEntities.remove(new LinkPos(a.toNodePos(), b.toNodePos(), key));
        if (entity != null) {
            entity.onDelete();
        }

        if (!linkRemoved) return false;

        world.scheduleCallbackUpdate(a);
        world.scheduleCallbackUpdate(b);

        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onUnlink(a, b, entity);
        }

        world.markDirty(id);

        return true;
    }

    void merge(@NotNull SimpleBlockGraph other) {
        if (other.id == id) {
            // we cannot merge with ourselves
            return;
        }

        // add our graph to all the positions and chunks the other graph is in
        for (var node : other.graph) {
            world.putGraphWithNode(id, new NodePos(node.data().getPos(), node.data().getNode()));

            // might as well set the node's graph id here as well
            node.data().graphId = id;
        }

        graph.join(other.graph);
        nodeEntities.putAll(other.nodeEntities);
        linkEntities.putAll(other.linkEntities);
        nodesInPos.putAll(other.nodesInPos);
        nodesToHolders.putAll(other.nodesToHolders);
        chunks.addAll(other.chunks);
        nodeTypeCache.clear();
        world.markDirty(id);

        // merge all our graph entities
        for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
            GraphEntityType<?> type = entry.getKey();
            GraphEntity<?> otherEntity = other.graphEntities.get(type);
            if (otherEntity != null) {
                type.merge(entry.getValue(), otherEntity);
            } else {
                GLLog.warn("Merging graph with missing graph entity: {}. Skipping...", type.id());
            }
        }

        // finally we destroy the old graph, removing it from all the graphs-in-pos and graphs-in-chunk trackers
        world.destroyGraph(other.id);
    }

    @NotNull List<SimpleBlockGraph> split() {
        var newGraphs = graph.split();

        if (!newGraphs.isEmpty()) {
            // collect the block-nodes, block-poses, and chunks we are no longer a part of
            Set<NodePos> removedNodes = new LinkedHashSet<>();
            Set<BlockPos> removedPoses = new LinkedHashSet<>();
            LongSet removedChunks = new LongLinkedOpenHashSet();

            for (Graph<SimpleNodeWrapper, LinkKey> graph : newGraphs) {
                for (var node : graph) {
                    BlockPos pos = node.data().getPos();
                    NodePos nodePos = new NodePos(pos, node.data().getNode());
                    removedNodes.add(nodePos);
                    removedPoses.add(pos);
                    removedChunks.add(ChunkSectionPos.from(pos).asLong());

                    // the node is in a new graph, so it obviously isn't in our graph anymore
                    nodesInPos.remove(pos, new SimpleNodeHolder<>(world.world, world, node));
                    nodesToHolders.remove(nodePos);
                }
            }

            // we aren't removing the blocks or chunks we still have
            for (var node : graph) {
                var data = node.data();
                removedPoses.remove(data.getPos());
                removedChunks.remove(ChunkSectionPos.from(data.getPos()).asLong());
            }

            // do this stuff instead of rebuilding-refs later
            world.removeGraphInPoses(id, removedNodes, removedPoses, removedChunks);
            chunks.removeAll(removedChunks);
            nodeTypeCache.clear();
            world.markDirty(id);

            // setup block-graphs for the newly created graphs
            List<SimpleBlockGraph> newBlockGraphs = new ArrayList<>(newGraphs.size());

            for (Graph<SimpleNodeWrapper, LinkKey> graph : newGraphs) {
                // create the new graph and set its nodes correctly
                SimpleBlockGraph bg = world.createGraph(false);
                bg.graph.join(graph);

                // this sets the nodes' graph ids, and sets up the new block-graph's chunks and nodes-in-pos
                bg.rebuildRefs();

                for (var node : bg.graph) {
                    NodePos key = new NodePos(node.data().getPos(), node.data().getNode());

                    // Add the new graph to the graphs-in-chunks and graphs-in-poses trackers.
                    // I considered trying to group block-poses by chunk to avoid duplicate look-ups, but it didn't look
                    // like it was worth the extra computation.
                    world.putGraphWithNode(bg.id, key);

                    // make sure to move the node entities over too
                    NodeEntity entity = nodeEntities.remove(key);
                    if (entity != null) {
                        bg.nodeEntities.put(key, entity);
                    }

                    // make sure to move link entities over too
                    for (var link : node.connections()) {
                        Node<SimpleNodeWrapper, LinkKey> other = link.other(node);
                        LinkPos linkKey =
                            new LinkPos(key, new NodePos(other.data().getPos(), other.data().getNode()), link.key());
                        LinkEntity linkEntity = linkEntities.remove(linkKey);
                        if (linkEntity != null) {
                            bg.linkEntities.put(linkKey, linkEntity);
                        }
                    }
                }

                // Split the graph entity
                for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
                    GraphEntityType<?> type = entry.getKey();
                    bg.graphEntities.put(type, type.splitNew(entry.getValue(), this,
                        new SimpleGraphEntityContext(world.world, world, bg)));
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
        for (NodeEntity entity : nodeEntities.values()) {
            entity.onUnload();
        }
        for (LinkEntity entity : linkEntities.values()) {
            entity.onUnload();
        }
        for (GraphEntity<?> entity : graphEntities.values()) {
            entity.onUnload();
        }
    }

    void onDestroy() {
        for (GraphEntity<?> entity : graphEntities.values()) {
            entity.onDestroy();
        }
    }

    void onTick() {
        for (GraphEntity<?> entity : graphEntities.values()) {
            entity.onTick();
        }
    }
}
