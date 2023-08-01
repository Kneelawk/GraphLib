package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Graph;
import com.kneelawk.graphlib.api.util.graph.Link;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.net.GLNet;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

/**
 * Holds and manages a set of block nodes.
 */
public class SimpleBlockGraph implements BlockGraph {
    static @NotNull SimpleBlockGraph fromTag(@NotNull SimpleServerGraphWorld controller, long id,
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
                NodeEntity entity = null;
                if (com.contains("entityType", NbtElement.STRING_TYPE)) {
                    Identifier entityTypeId = new Identifier(com.getString("entityType"));
                    NodeEntityType type = controller.universe.getNodeEntityType(entityTypeId);
                    if (type != null) {
                        entity = type.getDecoder().decode(com.get("entity"));
                    } else {
                        GLLog.warn("Encountered Node Entity with unknown type id: {}", entityTypeId);
                    }
                }

                nodes.add(graph.createNode(node.getPos(), node.getNode(), entity, false));
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
                    LinkKeyType type = controller.universe.getLinkKeyType(keyTypeId);
                    if (type != null) {
                        LinkKey decodedKey = type.getDecoder().decode(linkTag.get("key"));
                        if (decodedKey != null) {
                            key = decodedKey;
                        }
                    } else {
                        GLLog.warn("Encountered link key with unknown type id: {}", keyTypeId);
                    }
                }

                LinkEntity entity = null;
                if (linkTag.contains("entityType", NbtElement.STRING_TYPE)) {
                    Identifier entityTypeId = new Identifier(linkTag.getString("entityType"));
                    LinkEntityType type = controller.universe.getLinkEntityType(entityTypeId);
                    if (type != null) {
                        entity = type.getDecoder().decode(linkTag.get("entity"));
                    } else {
                        GLLog.warn("Encountered Link Entity with unknown id: {}", entityTypeId);
                    }
                }

                graph.link(first, second, key, entity, false);
            }
        }

        // decode the graph entities
        for (GraphEntityType<?> type : controller.universe.getAllGraphEntityTypes()) {
            SimpleGraphEntityContext ctx = new SimpleGraphEntityContext(controller.world, controller, graph);
            if (graphEntities.contains(type.getId().toString(), NbtElement.COMPOUND_TYPE)) {
                NbtCompound entityCom = graphEntities.getCompound(type.getId().toString());
                GraphEntity<?> entity = type.getDecoder().decode(entityCom.get("entity"));
                if (entity == null) {
                    entity = type.getFactory().createNew();
                }
                graph.graphEntities.put(type, entity);
                entity.onInit(ctx);
            } else {
                GLLog.warn("Graph missing graph entity of type: {}, creating a new one...", type.getId());
                GraphEntity<?> entity = type.getFactory().createNew();
                graph.graphEntities.put(type, entity);
                entity.onInit(ctx);
            }
        }

        // no need to rebuild refs as that stuff is handled by graph.createNode(...)

        return graph;
    }

    final SimpleGraphCollection world;
    private final long id;

    private final Graph<SimpleNodeWrapper, LinkKey> graph = new Graph<>();
    private final Map<NodePos, NodeEntity> nodeEntities = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<LinkPos, LinkEntity> linkEntities = new Object2ObjectLinkedOpenHashMap<>();
    private final Multimap<BlockPos, NodeHolder<BlockNode>> nodesInPos = LinkedHashMultimap.create();
    private final Long2ObjectMap<Set<NodeHolder<BlockNode>>> nodesInChunk = new Long2ObjectLinkedOpenHashMap<>();
    private final Map<NodePos, NodeHolder<BlockNode>> nodesToHolders = new Object2ObjectLinkedOpenHashMap<>();
    final LongSet chunks = new LongLinkedOpenHashSet();
    private final Map<CacheCategory<?>, List<?>> nodeCaches = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<GraphEntityType<?>, GraphEntity<?>> graphEntities = new Object2ObjectLinkedOpenHashMap<>();

    public SimpleBlockGraph(@NotNull SimpleGraphCollection world, long id, boolean initializeGraphEntities) {
        this(world, id, LongSet.of());

        // When newly-creating a graph, mark it dirty, so it'll get saved.
        // If this is a throw-away graph, it should get absorbed and deleted before the next tick.
        this.world.markDirty(id);

        // Add all the empty graph entities
        if (initializeGraphEntities) {
            for (GraphEntityType<?> type : this.world.getUniverse().getAllGraphEntityTypes()) {
                GraphEntity<?> entity = type.getFactory().createNew();
                graphEntities.put(type, entity);
                entity.onInit(new SimpleGraphEntityContext(this.world.getWorld(), this.world, this));
            }
        }
    }

    private SimpleBlockGraph(@NotNull SimpleGraphCollection world, long id, @NotNull LongSet chunks) {
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
                com.putString("entityType", entity.getType().getId().toString());
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
            linkTag.putString("keyType", key.getType().getId().toString());
            NbtElement keyTag = key.toTag();
            if (keyTag != null) {
                linkTag.put("key", keyTag);
            }

            LinkEntity entity = linkEntities.get(
                new LinkPos(link.first().data().getPos(), link.first().data().getNode(), link.second().data().getPos(),
                    link.second().data().getNode(), link.key()));
            if (entity != null) {
                linkTag.putString("entityType", entity.getType().getId().toString());
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

            graphEntitiesCom.put(entry.getKey().getId().toString(), graphEntityCom);
        }

        tag.put("graphEntities", graphEntitiesCom);

        return tag;
    }

    void loadGraphEntitiesFromPacket(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        int entityCount = buf.readVarUnsignedInt();
        for (int entityIndex = 0; entityIndex < entityCount; entityIndex++) {
            int typeIdInt = buf.readVarUnsignedInt();
            Identifier typeId = GLNet.ID_CACHE.getObj(ctx.getConnection(), typeIdInt);
            if (typeId == null) {
                GLLog.warn("Unable to decode graph entity type id int as id. Int: {}", typeIdInt);
                throw new InvalidInputDataException(
                    "Unable to decode graph entity type id int as id. Int: " + typeIdInt);
            }

            GraphEntityType<?> type = world.getUniverse().getGraphEntityType(typeId);
            if (type == null) {
                GLLog.warn("Received unknown graph entity type id: {}", typeId);
                throw new InvalidInputDataException("Received unknown graph entity type id: " + typeId);
            }

            GraphEntityPacketDecoder decoder = type.getPacketDecoder();
            if (decoder == null) {
                GLLog.warn("Received graph entity but type has no packet decoder. Id: {}", typeId);
                throw new InvalidInputDataException(
                    "Received graph entity but type has no packet decoder. Id: " + typeId);
            }

            GraphEntity<?> entity = decoder.decode(buf, ctx);

            if (graphEntities.containsKey(type)) {
                entity.onDiscard();
            } else {
                graphEntities.put(type, entity);
                entity.onInit(new SimpleGraphEntityContext(world.getWorld(), world, this));
            }
        }

        for (GraphEntityType<?> type : world.getUniverse().getAllGraphEntityTypes()) {
            if (!graphEntities.containsKey(type)) {
                GraphEntity<?> entity = type.getFactory().createNew();
                graphEntities.put(type, entity);
                entity.onInit(new SimpleGraphEntityContext(world.getWorld(), world, this));
            }
        }
    }

    void writeGraphEntitiesToPacket(NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedInt(graphEntities.size());
        for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
            buf.writeVarUnsignedInt(GLNet.ID_CACHE.getId(ctx.getConnection(), entry.getKey().getId()));

            entry.getValue().toPacket(buf, ctx);
        }
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
     * Gets the graph view that this graph exists within.
     *
     * @return the graph view that this graph exists within.
     */
    @Override
    public GraphView getGraphView() {
        return world;
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

        return new SimpleLinkHolder<>(world.getWorld(), world, rawLink);
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
     * Gets all the nodes in the given chunk section.
     *
     * @param pos the position of the chunk section to get all nodes from.
     * @return a stream of all nodes in the given chunk section.
     */
    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodesInChunkSection(ChunkSectionPos pos) {
        Set<NodeHolder<BlockNode>> inChunk = nodesInChunk.get(pos.asLong());
        if (inChunk != null) {
            return inChunk.stream();
        } else {
            return Stream.empty();
        }
    }

    /**
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodes() {
        return graph.stream().map(node -> new SimpleNodeHolder<>(world.getWorld(), world, node));
    }

    /**
     * Gets all node entities in this graph.
     *
     * @return a stream of all node entities in this graph.
     */
    @Override
    public @NotNull Stream<NodeEntity> getNodeEntities() {
        return nodeEntities.values().stream();
    }

    /**
     * Gets all link entities in this graph.
     *
     * @return a stream of all link entities in this graph.
     */
    @Override
    public @NotNull Stream<LinkEntity> getLinkEntities() {
        return linkEntities.values().stream();
    }

    /**
     * Gets all nodes in this graph that match the given cache category.
     *
     * @param category the category of the cache to retrieve.
     * @return all nodes in this graph that match the given cache category.
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T extends BlockNode> Collection<NodeHolder<T>> getCachedNodes(@NotNull CacheCategory<T> category) {
        List<NodeHolder<T>> cached = (List<NodeHolder<T>>) nodeCaches.get(category);
        if (cached == null) {
            ImmutableList.Builder<NodeHolder<T>> builder = ImmutableList.builder();
            for (Node<SimpleNodeWrapper, LinkKey> node : graph) {
                SimpleNodeHolder<?> holder = new SimpleNodeHolder<>(world.getWorld(), world, node);
                if (category.matches(holder)) {
                    builder.add(holder.cast(category.getNodeClass()));
                }
            }
            cached = builder.build();
            nodeCaches.put(category, cached);
        }
        return cached;
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
        if (entity == null)
            throw new IllegalArgumentException("No graph entity type registered with id: " + type.getId());
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
        nodesInChunk.clear();
        nodesToHolders.clear();
        world.markDirty(id);
        for (var node : graph) {
            SimpleNodeWrapper data = node.data();
            data.graphId = id;
            BlockPos pos = data.getPos();
            long sectionPos = ChunkSectionPos.from(pos).asLong();
            chunks.add(sectionPos);
            NodeHolder<BlockNode> holder = new SimpleNodeHolder<>(world.getWorld(), world, node);
            nodesInPos.put(pos, holder);
            nodesInChunk.computeIfAbsent(sectionPos, posLong -> new ObjectLinkedOpenHashSet<>()).add(holder);
            nodesToHolders.put(holder.getPos(), holder);
        }
    }

    private void rebuildCaches() {
        nodeCaches.clear();
        for (CacheCategory<?> category : world.getUniverse().getCacheCatetories()) {
            // get the cache the first time, building the cache
            getCachedNodes(category);
        }
    }

    @NotNull SimpleNodeHolder<BlockNode> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node,
                                                    @Nullable NodeEntity entity, boolean newlyAdded) {
        BlockPos pos = blockPos.toImmutable();
        NodePos nodePos = new NodePos(pos, node);

        SimpleNodeHolder<BlockNode> graphNode = new SimpleNodeHolder<>(world.getWorld(), world,
            graph.add(new SimpleNodeWrapper(pos, node, id)));

        // Get the proper node entity and determine whether it needs to be initialized
        NodeEntity nodeEntity;
        boolean initialize;
        if (entity != null) {
            if (node.shouldHaveNodeEntity(graphNode) && !nodeEntities.containsKey(nodePos)) {
                nodeEntities.put(nodePos, entity);
                nodeEntity = entity;
                initialize = true;
            } else {
                entity.onDiscard();
                nodeEntity = nodeEntities.get(nodePos);
                initialize = false;
            }
        } else {
            if (node.shouldHaveNodeEntity(graphNode) && !nodeEntities.containsKey(nodePos)) {
                nodeEntity = node.createNodeEntity(graphNode);
                if (nodeEntity != null) {
                    nodeEntities.put(nodePos, nodeEntity);
                    initialize = true;
                } else {
                    initialize = false;
                }
            } else {
                nodeEntity = nodeEntities.get(nodePos);
                initialize = false;
            }
        }

        nodesInPos.put(pos, graphNode);
        long sectionPos = ChunkSectionPos.from(pos).asLong();
        nodesInChunk.computeIfAbsent(sectionPos, posLong -> new ObjectLinkedOpenHashSet<>()).add(graphNode);
        nodesToHolders.put(nodePos, graphNode);
        chunks.add(sectionPos);
        world.putGraphWithNode(id, nodePos);
        world.scheduleCallbackUpdate(graphNode, true);

        rebuildCaches();

        if (initialize) {
            nodeEntity.onInit(new SimpleNodeEntityContext(graphNode, world.getWorld(), world));

            if (newlyAdded) {
                nodeEntity.onAdded();
            } else {
                nodeEntity.onLoaded();
            }
        }

        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onNodeCreated(graphNode, nodeEntity);
        }

        world.markDirty(id);

        world.sendNodeAdd(this, graphNode);

        return graphNode;
    }

    void destroyNode(@NotNull NodeHolder<BlockNode> holder, boolean doSplit) {
        // send the node remove packet before any of the removing has actually happened
        world.sendNodeRemove(this, holder);

        // see if removing this node means removing a block-pos or a chunk
        SimpleNodeHolder<BlockNode> node = (SimpleNodeHolder<BlockNode>) holder;
        NodePos removedNode = node.getPos();
        BlockPos removedPos = node.getBlockPos();
        ChunkSectionPos removedChunk = ChunkSectionPos.from(removedPos);
        nodesInPos.remove(removedPos, node);
        Set<NodeHolder<BlockNode>> inRemovedChunk = nodesInChunk.get(removedChunk.asLong());
        if (inRemovedChunk != null) {
            inRemovedChunk.remove(holder);
            if (inRemovedChunk.isEmpty()) nodesInChunk.remove(removedChunk.asLong());
        }
        nodesToHolders.remove(removedNode);
        world.markDirty(id);

        Map<LinkPos, LinkEntity> removedLinks = new Object2ObjectLinkedOpenHashMap<>();

        // schedule updates for each of the node's connected nodes while collecting removed connections
        for (Link<SimpleNodeWrapper, LinkKey> link : node.node.connections()) {
            // scheduled updates happen after, so we don't need to worry whether the node's been removed from the graph
            // yet, as it will be when these updates are actually applied
            world.scheduleCallbackUpdate(
                new SimpleNodeHolder<>(world.getWorld(), world, link.other(node.node)), true);

            // collect the link entities to be removed
            LinkPos linkKey =
                new LinkPos(link.first().data().getPos(), link.first().data().getNode(), link.second().data().getPos(),
                    link.second().data().getNode(), link.key());
            LinkEntity linkEntity = linkEntities.get(linkKey);
            if (linkEntity != null) {
                removedLinks.put(linkKey, linkEntity);
            }
        }
        world.scheduleCallbackUpdate(node, false);

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
        NodeEntity nodeEntity = nodeEntities.remove(node.getPos());
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

        rebuildCaches();

        if (graph.isEmpty()) {
            // This only happens if this graph contained a single node before and that node has now been removed.
            world.destroyGraph(id);
        } else if (doSplit) {
            // Split leaves both new graphs and this graph in valid states as far as refs go.
            // Also, split is guaranteed not to leave this graph empty.
            split();
        }
    }

    @NotNull LinkHolder<LinkKey> link(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key,
                                      @Nullable LinkEntity entity, boolean newlyAdded) {
        LinkHolder<LinkKey> link = new SimpleLinkHolder<>(world.getWorld(), world,
            graph.link(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node, key));
        LinkPos linkPos = link.getPos();

        // Get the proper node entity and determine whether it needs to be initialized
        LinkEntity linkEntity;
        boolean initialize;
        if (entity != null) {
            if (key.shouldHaveLinkEntity(link) && !linkEntities.containsKey(linkPos)) {
                linkEntities.put(linkPos, entity);
                linkEntity = entity;
                initialize = true;
            } else {
                entity.onDiscard();
                linkEntity = linkEntities.get(linkPos);
                initialize = false;
            }
        } else {
            if (key.shouldHaveLinkEntity(link) && !linkEntities.containsKey(linkPos)) {
                linkEntity = key.createLinkEntity(link);
                if (linkEntity != null) {
                    linkEntities.put(linkPos, linkEntity);
                    initialize = true;
                } else {
                    initialize = false;
                }
            } else {
                linkEntity = linkEntities.get(linkPos);
                initialize = false;
            }
        }

        world.scheduleCallbackUpdate(a, true);
        world.scheduleCallbackUpdate(b, true);

        if (initialize) {
            linkEntity.onInit(new SimpleLinkEntityContext(link, world.getWorld(), world));

            if (newlyAdded) {
                linkEntity.onAdded();
            } else {
                linkEntity.onLoaded();
            }
        }

        for (GraphEntity<?> graphEntity : graphEntities.values()) {
            graphEntity.onLink(a, b, linkEntity);
        }

        world.markDirty(id);

        world.sendLink(this, link);

        return link;
    }

    boolean unlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key) {
        world.sendUnlink(this, a, b, key);

        boolean linkRemoved =
            graph.unlink(((SimpleNodeHolder<BlockNode>) a).node, ((SimpleNodeHolder<BlockNode>) b).node, key);

        LinkEntity entity = linkEntities.remove(new LinkPos(a.getPos(), b.getPos(), key));
        if (entity != null) {
            entity.onDelete();
        }

        if (!linkRemoved) return false;

        world.scheduleCallbackUpdate(a, true);
        world.scheduleCallbackUpdate(b, true);

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

        world.sendMerge(other, this);

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
        for (Long2ObjectMap.Entry<Set<NodeHolder<BlockNode>>> entry : other.nodesInChunk.long2ObjectEntrySet()) {
            nodesInChunk.merge(entry.getLongKey(), entry.getValue(), (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        nodesToHolders.putAll(other.nodesToHolders);
        chunks.addAll(other.chunks);
        world.markDirty(id);

        // merge all our graph entities
        for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
            GraphEntityType<?> type = entry.getKey();
            GraphEntity<?> otherEntity = other.graphEntities.get(type);
            if (otherEntity != null) {
                type.merge(entry.getValue(), otherEntity);
            } else {
                GLLog.warn("Merging graph with missing graph entity: {}. Skipping...", type.getId());
            }
        }

        rebuildCaches();

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
                    long sectionPos = ChunkSectionPos.from(pos).asLong();
                    removedChunks.add(sectionPos);

                    // the node is in a new graph, so it obviously isn't in our graph anymore
                    NodeHolder<BlockNode> holder = new SimpleNodeHolder<>(world.getWorld(), world, node);
                    nodesInPos.remove(pos, holder);
                    Set<NodeHolder<BlockNode>> inRemovedChunk = nodesInChunk.get(sectionPos);
                    if (inRemovedChunk != null) {
                        inRemovedChunk.remove(holder);
                        if (inRemovedChunk.isEmpty()) nodesInChunk.remove(sectionPos);
                    }
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
                    GraphEntity<?> entity = type.splitNew(entry.getValue(), this, bg);
                    bg.graphEntities.put(type, entity);
                    entity.onInit(new SimpleGraphEntityContext(world.getWorld(), world, bg));
                }

                // we want to rebuild caches after entities have been moved
                bg.rebuildCaches();

                newBlockGraphs.add(bg);

                // Fire update events for the new graphs
                world.graphUpdated(bg);

                world.sendSplitInto(this, bg);
            }

            rebuildCaches();

            // Fire the update events
            world.graphUpdated(this);

            return newBlockGraphs;
        } else {
            // Fire the update events
            world.graphUpdated(this);

            return List.of();
        }
    }

    void splitInto(SimpleBlockGraph into, Collection<NodePos> nodes) {
        // collect the block-nodes, block-poses, and chunks we are no longer a part of
        Set<Node<SimpleNodeWrapper, LinkKey>> movedNodes = new LinkedHashSet<>();
        Set<NodePos> removedNodes = new LinkedHashSet<>();
        Set<BlockPos> removedPoses = new LinkedHashSet<>();
        LongSet removedChunks = new LongLinkedOpenHashSet();

        for (var nodePos : nodes) {
            // the node is in a new graph, so it obviously isn't in our graph anymore
            NodeHolder<BlockNode> holder = nodesToHolders.remove(nodePos);
            if (holder != null) {
                BlockPos pos = nodePos.pos();
                removedNodes.add(nodePos);
                removedPoses.add(pos);
                long sectionPos = ChunkSectionPos.from(pos).asLong();
                removedChunks.add(sectionPos);

                nodesInPos.remove(pos, holder);
                Set<NodeHolder<BlockNode>> inRemovedChunk = nodesInChunk.get(sectionPos);
                if (inRemovedChunk != null) {
                    inRemovedChunk.remove(holder);
                    if (inRemovedChunk.isEmpty()) nodesInChunk.remove(sectionPos);
                }

                movedNodes.add(((SimpleNodeHolder<BlockNode>) holder).node);
            }
        }

        // return if nothing is actually going to be moved
        if (movedNodes.isEmpty()) return;

        // Actually move the nodes
        graph.moveBulkUnchecked(into.graph, movedNodes);

        // we aren't removing the blocks or chunks we still have
        for (var node : graph) {
            var data = node.data();
            removedPoses.remove(data.getPos());
            removedChunks.remove(ChunkSectionPos.from(data.getPos()).asLong());
        }

        // do this stuff instead of rebuilding-refs later
        world.removeGraphInPoses(id, removedNodes, removedPoses, removedChunks);
        chunks.removeAll(removedChunks);
        world.markDirty(id);

        // this sets the nodes' graph ids, and sets up the new block-graph's chunks and nodes-in-pos
        into.rebuildRefs();

        for (var node : into.graph) {
            NodePos key = new NodePos(node.data().getPos(), node.data().getNode());

            // Add the new graph to the graphs-in-chunks and graphs-in-poses trackers.
            // I considered trying to group block-poses by chunk to avoid duplicate look-ups, but it didn't look
            // like it was worth the extra computation.
            world.putGraphWithNode(into.id, key);

            // make sure to move the node entities over too
            NodeEntity entity = nodeEntities.remove(key);
            if (entity != null) {
                into.nodeEntities.put(key, entity);
            }

            // make sure to move link entities over too
            for (var link : node.connections()) {
                Node<SimpleNodeWrapper, LinkKey> other = link.other(node);
                LinkPos linkKey =
                    new LinkPos(key, new NodePos(other.data().getPos(), other.data().getNode()), link.key());
                LinkEntity linkEntity = linkEntities.remove(linkKey);
                if (linkEntity != null) {
                    into.linkEntities.put(linkKey, linkEntity);
                }
            }
        }

        // Split the graph entity
        for (Map.Entry<GraphEntityType<?>, GraphEntity<?>> entry : graphEntities.entrySet()) {
            GraphEntityType<?> type = entry.getKey();
            GraphEntity<?> entity = type.splitNew(entry.getValue(), this, into);
            into.graphEntities.put(type, entity);
            entity.onInit(new SimpleGraphEntityContext(world.getWorld(), world, into));
        }

        // we want to rebuild caches after entities have been moved
        into.rebuildCaches();
    }

    void unloadInChunk(int chunkX, int chunkZ) {
        // collect the block-nodes, block-poses, and chunks we are no longer a part of
        Set<NodePos> removedNodes = new LinkedHashSet<>();
        Set<BlockPos> removedPoses = new LinkedHashSet<>();
        LongSet removedChunks = new LongLinkedOpenHashSet();

        for (int sectionY = world.getWorld().getBottomSectionCoord();
             sectionY < world.getWorld().getTopSectionCoord(); sectionY++) {
            long longPos = ChunkSectionPos.asLong(chunkX, sectionY, chunkZ);
            Set<NodeHolder<BlockNode>> inRemovedChunk = nodesInChunk.get(longPos);
            if (inRemovedChunk != null) {
                removedChunks.add(longPos);

                for (var holder : inRemovedChunk) {
                    NodePos nodePos = holder.getPos();
                    removedNodes.add(nodePos);
                    removedPoses.add(nodePos.pos());

                    // call onUnload
                    NodeEntity nodeEntity = nodeEntities.get(nodePos);
                    if (nodeEntity != null) {
                        nodeEntity.onUnload();
                    }

                    // call onUnload
                    for (LinkHolder<LinkKey> link : holder.getConnections()) {
                        LinkPos linkKey = link.getPos();
                        LinkEntity linkEntity = linkEntities.remove(linkKey);
                        if (linkEntity != null) {
                            linkEntity.onUnload();
                        }
                    }

                    // in this case, unloading means removing
                    graph.remove(((SimpleNodeHolder<BlockNode>) holder).node);
                    nodesInPos.removeAll(nodePos.pos());
                    nodesToHolders.remove(nodePos);
                    nodeEntities.remove(nodePos);
                }

                nodesInChunk.remove(longPos);
            }
        }

        chunks.removeAll(removedChunks);
        rebuildCaches();

        world.removeGraphInPoses(id, removedNodes, removedPoses, removedChunks);
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
