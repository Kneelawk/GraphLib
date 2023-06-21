/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityFactory;
import com.kneelawk.graphlib.api.graph.user.LinkEntityPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityFactory;
import com.kneelawk.graphlib.api.graph.user.NodeEntityPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.impl.net.GLNet;

public class SimpleClientGraphWorld implements GraphView, ClientGraphWorldImpl, SimpleGraphCollection {
    private final SimpleGraphUniverse universe;
    final World world;

    private final SimpleClientGraphChunkManager manager;
    private final Long2ObjectMap<SimpleBlockGraph> graphs = new Long2ObjectLinkedOpenHashMap<>();

    public SimpleClientGraphWorld(SimpleGraphUniverse universe, World world, int loadDistance) {
        this.universe = universe;
        this.world = world;

        manager = new SimpleClientGraphChunkManager(loadDistance, world, this::onUnload);
    }

    @Override
    public void unload(int chunkX, int chunkZ) {
        manager.unload(chunkX, chunkZ);
    }

    @Override
    public void setChunkMapCenter(int x, int z) {
        manager.setPillarMapCenter(x, z);
    }

    @Override
    public void updateLoadDistance(int loadDistance) {
        manager.updateLoadDistance(loadDistance);
    }

    @Override
    public void readChunkPillar(int chunkX, int chunkZ, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        SimpleBlockGraphPillar pillar = manager.getOrCreatePillar(chunkX, chunkZ);
        if (pillar == null) {
            GLLog.warn("Received pillar outside current client range at ({}, {})", chunkX, chunkZ);
            ctx.drop("Pillar outside range");
            return;
        }

        int graphCount = buf.readVarUnsignedInt();
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++) {
            buf.readMarker("graph_start");

            long graphId = buf.readVarUnsignedLong();
            SimpleBlockGraph graph = getOrCreateGraph(graphId);

            // load graph entities if any exist
            graph.loadGraphEntitiesFromPacket(buf, ctx);

            List<NodeHolder<BlockNode>> nodeList = new ObjectArrayList<>();

            buf.readMarker("nodes_start");

            int nodeCount = buf.readInt();
            for (int i = 0; i < nodeCount; i++) {
                // decode block node
                NodePos nodePos = NodePos.fromPacket(buf, ctx, universe);

                BlockNode node = nodePos.node();
                BlockPos blockPos = nodePos.pos();

                // decode node entity
                NodeEntityFactory entityFactory =
                    readNodeEntity(ctx, buf, node, blockPos);

                NodeHolder<BlockNode> holder = graph.createNode(blockPos, node, entityFactory);
                nodeList.add(holder);
            }

            buf.readMarker("i_links_start");

            // decode internal links
            int linkCount = buf.readInt();
            for (int i = 0; i < linkCount; i++) {
                int nodeAIndex = buf.readVarUnsignedInt();
                int nodeBIndex = buf.readVarUnsignedInt();

                if (nodeAIndex < 0 || nodeAIndex >= nodeList.size()) {
                    GLLog.warn("Received packet with invalid links. Node index {} is invalid.", nodeAIndex);
                    // packet is foo bar
                    throw new InvalidInputDataException(
                        "Received packet with invalid links. Node index " + nodeAIndex + " is invalid.");
                }

                if (nodeBIndex < 0 || nodeBIndex >= nodeList.size()) {
                    GLLog.warn("Received packet with invalid links. Node index {} is invalid.", nodeBIndex);
                    // packet is foo bar
                    throw new InvalidInputDataException(
                        "Received packet with invalid links. Node index " + nodeBIndex + " is invalid.");
                }

                NodeHolder<BlockNode> nodeA = nodeList.get(nodeAIndex);
                NodeHolder<BlockNode> nodeB = nodeList.get(nodeBIndex);

                // decode link key
                LinkKeyType linkType =
                    GLNet.readType(buf, ctx.getConnection(), universe::getLinkKeyType, "LinkKey",
                        nodeA.getBlockPos());

                LinkKeyPacketDecoder linkDecoder = linkType.getPacketDecoder();
                if (linkDecoder == null) {
                    GLLog.error("Unable to decode LinkKey {} @ {}-{} because it has no packet decoder",
                        linkType.getId(), nodeA.getBlockPos(), nodeB.getBlockPos());
                    throw new InvalidInputDataException(
                        "Unable to decode LinkKey " + linkType.getId() + " @ " + nodeA.getBlockPos() + "-" +
                            nodeB.getBlockPos() + " because it has no packet decoder");
                }

                LinkKey linkKey = linkDecoder.decode(buf, ctx);

                // decode link entity
                LinkEntityFactory entityFactory =
                    readLinkEntity(buf, ctx, new LinkPos(nodeA.getPos(), nodeB.getPos(), linkKey));

                graph.link(nodeA, nodeB, linkKey, entityFactory);
            }

            buf.readMarker("e_links_start");

            // decode external links
            record ELink(@Nullable NodeHolder<BlockNode> holderA, @Nullable NodeHolder<BlockNode> holderB, LinkKey key,
                         LinkEntityFactory entityFactory) {}

            int eLinkCount = buf.readVarUnsignedInt();
            List<ELink> eLinks = new ArrayList<>(eLinkCount);
            for (int i = 0; i < eLinkCount; i++) {
                LinkPos link = LinkPos.fromPacket(buf, ctx, universe);

                NodeHolder<BlockNode> holderA = graph.getNodeAt(link.first());
                NodeHolder<BlockNode> holderB = graph.getNodeAt(link.second());

                // read quarantined link entity
                LinkEntityFactory entityFactory = readLinkEntity(buf, ctx, link);

                eLinks.add(new ELink(holderA, holderB, link.key(), entityFactory));
            }

            for (ELink link : eLinks) {
                if (link.holderA == null || link.holderB == null) {
                    // ignore links with missing nodes,
                    // they'll just happen sometimes because the server will send links to nodes we don't know about
                    continue;
                }

                graph.link(link.holderA, link.holderB, link.key, link.entityFactory);
            }

            buf.readMarker("graph_end");
        }
    }

    @NotNull
    private NodeEntityFactory readNodeEntity(IMsgReadCtx ctx, NetByteBuf graphBuf, BlockNode node,
                                             BlockPos blockPos) throws InvalidInputDataException {
        NodeEntityFactory entityFactory = node::createNodeEntity;
        // quarantine node entities, because they cannot be validated
        int entityBufLen = graphBuf.readVarUnsignedInt();
        if (entityBufLen > 0) {
            NetByteBuf entityBuf = NetByteBuf.buffer();
            graphBuf.readBytes(entityBuf, entityBufLen);

            NodeEntityType entityType =
                GLNet.readType(entityBuf, ctx.getConnection(), universe::getNodeEntityType, "NodeEntity",
                    blockPos);
            NodeEntityPacketDecoder entityDecoder = entityType.getPacketDecoder();
            if (entityDecoder != null) {
                entityFactory = entityCtx -> entityDecoder.decode(entityBuf, ctx, entityCtx);
            } else {
                GLLog.error("Unable to decode NodeEntity {} @ {} because it has no packet decoder",
                    entityType.getId(), blockPos);
                throw new InvalidInputDataException(
                    "Unable to decode NodeEntity " + entityType.getId() + " @ " + blockPos +
                        " because it has no packet decoder");
            }
        }
        return entityFactory;
    }

    @NotNull
    private LinkEntityFactory readLinkEntity(NetByteBuf buf, IMsgReadCtx ctx, LinkPos link)
        throws InvalidInputDataException {
        LinkEntityFactory entityFactory = link.key()::createLinkEntity;
        // quarantine link entities for the same reason a node entities
        int entityBufLen = buf.readVarUnsignedInt();
        if (entityBufLen > 0) {
            NetByteBuf entityBuf = NetByteBuf.buffer();
            buf.readBytes(entityBuf, entityBufLen);

            LinkEntityType entityType =
                GLNet.readType(entityBuf, ctx.getConnection(), universe::getLinkEntityType, "LinkEntity",
                    link.first().pos());
            LinkEntityPacketDecoder entityDecoder = entityType.getPacketDecoder();
            if (entityDecoder != null) {
                entityFactory = entityCtx -> entityDecoder.decode(entityBuf, ctx, entityCtx);
            } else {
                GLLog.error("Unable to decode LinkEntity {} @ {} because it has no packet decoder",
                    entityType.getId(), link);
                throw new InvalidInputDataException("Unable to decode LinkEntity " + entityType.getId() + " @ " + link +
                    " because it has no packet decoder");
            }
        }
        return entityFactory;
    }

    @Override
    public void readNodeAdd(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        NodePos pos = NodePos.fromPacket(buf, ctx, universe);

        BlockNode node = pos.node();
        BlockPos blockPos = pos.pos();

        ChunkPos chunkPos = new ChunkPos(pos.pos());
        if (!manager.isInRadius(chunkPos)) {
            GLLog.warn("Received node add @ {} that is outside client chunk radius", pos);
            ctx.drop("Received node add outside client chunk radius");
            return;
        }

        long graphId = buf.readVarUnsignedLong();

        SimpleBlockGraph graph = getOrCreateGraph(graphId);

        graph.loadGraphEntitiesFromPacket(buf, ctx);

        // decode node entity
        NodeEntityFactory entityFactory = readNodeEntity(ctx, buf, node, blockPos);

        graph.createNode(blockPos, node, entityFactory);
    }

    @Override
    public void readMerge(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        long fromId = buf.readVarUnsignedLong();
        SimpleBlockGraph from = graphs.get(fromId);
        if (from == null) {
            // we don't know the graph being merged from, so we can safely ignore this packet
            ctx.drop("Unknown from graph");
            return;
        }

        long intoId = buf.readVarUnsignedLong();
        // however, it is possible for a graph we do know about to get merged into one we don't know about yet
        SimpleBlockGraph into = getOrCreateGraph(intoId);

        // initialize into's graph entities if we haven't already
        into.loadGraphEntitiesFromPacket(buf, ctx);

        // do the merge
        into.merge(from);
    }

    @Override
    public void readLink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        long graphId = buf.readVarUnsignedLong();
        SimpleBlockGraph graph = graphs.get(graphId);
        if (graph == null) {
            GLLog.warn("Received link in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        LinkPos linkPos = LinkPos.fromPacket(buf, ctx, universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            ctx.drop("Link outside range");
            return;
        }

        LinkEntityFactory entityFactory = readLinkEntity(buf, ctx, linkPos);

        graph.link(nodeA, nodeB, linkPos.key(), entityFactory);
    }

    @Override
    public void readUnlink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        long graphId = buf.readVarUnsignedLong();
        SimpleBlockGraph graph = graphs.get(graphId);
        if (graph == null) {
            GLLog.warn("Received unlink in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        LinkPos linkPos = LinkPos.fromPacket(buf, ctx, universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            ctx.drop("Link outside range");
            return;
        }

        graph.unlink(nodeA, nodeB, linkPos.key());
    }

    @Override
    public void readSplitInto(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        long fromId = buf.readVarUnsignedLong();
        SimpleBlockGraph from = graphs.get(fromId);
        if (from == null) {
            // we don't know the graph being split from, so we can safely ignore this packet
            ctx.drop("Unknown from graph");
            return;
        }

        long intoId = buf.readVarUnsignedLong();
        // however, the into graph is normally a newly created one
        SimpleBlockGraph into = getOrCreateGraph(intoId);

        // initialize into's graph entities
        into.loadGraphEntitiesFromPacket(buf, ctx);

        // load the nodes to be split off
        Set<NodePos> toSplit = new ObjectLinkedOpenHashSet<>();
        int nodeCount = buf.readVarUnsignedInt();
        for (int i = 0; i < nodeCount; i++) {
            NodePos pos = NodePos.fromPacket(buf, ctx, universe);

            toSplit.add(pos);
        }

        // Split Into only moves nodes from actually knows about, so nodes that are outside the client radius get
        // discarded.
        from.splitInto(into, toSplit);
    }

    @Override
    public void readNodeRemove(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        long graphId = buf.readVarUnsignedLong();
        SimpleBlockGraph graph = graphs.get(graphId);
        if (graph == null) {
            GLLog.warn("Received node remove in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        NodePos pos = NodePos.fromPacket(buf, ctx, universe);

        NodeHolder<BlockNode> node = graph.getNodeAt(pos);
        // ignore removals of nodes we don't know about
        if (node == null) return;

        graph.destroyNode(node, false);
    }

    @Override
    public @NotNull GraphUniverse getUniverse() {
        return universe;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos) {
        return getAllGraphIdsAt(pos).mapToObj(graphs).filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
    }

    @Override
    public @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos) {
        return getAllGraphIdsAt(pos.pos()).mapToObj(graphs).filter(Objects::nonNull).flatMap(g -> g.getNodesAt(pos));
    }

    @Override
    public @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos) {
        BlockGraph graph = getGraphForNode(pos);
        if (graph == null) return null;

        return graph.getNodeAt(pos);
    }

    @Override
    public boolean nodeExistsAt(@NotNull NodePos pos) {
        SimpleBlockGraphChunk chunk = manager.getIfExists(ChunkSectionPos.from(pos.pos()));
        if (chunk == null) return false;

        return chunk.containsNode(pos, graphs);
    }

    @Override
    public @Nullable BlockGraph getGraphForNode(@NotNull NodePos pos) {
        SimpleBlockGraphChunk chunk = manager.getIfExists(ChunkSectionPos.from(pos.pos()));
        if (chunk == null) return null;

        return chunk.getGraphForNode(pos, graphs);
    }

    @Override
    public @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos) {
        BlockGraph graph = getGraphForNode(pos);
        if (graph == null) return null;
        return graph.getNodeEntity(pos);
    }

    @Override
    public boolean linkExistsAt(@NotNull LinkPos pos) {
        BlockGraph graph = getGraphForNode(pos.first());
        if (graph == null) return false;
        return graph.linkExistsAt(pos);
    }

    @Override
    public @Nullable LinkHolder<LinkKey> getLinkAt(@NotNull LinkPos pos) {
        BlockGraph graph = getGraphForNode(pos.first());
        if (graph == null) return null;
        return graph.getLinkAt(pos);
    }

    @Override
    public @Nullable LinkEntity getLinkEntity(@NotNull LinkPos pos) {
        BlockGraph graph = getGraphForNode(pos.first());
        if (graph == null) return null;
        return graph.getLinkEntity(pos);
    }

    @Override
    public @NotNull LongStream getAllGraphIdsAt(@NotNull BlockPos pos) {
        SimpleBlockGraphChunk chunk = manager.getIfExists(ChunkSectionPos.from(pos));
        if (chunk == null) return LongStream.empty();

        return chunk.getGraphsAt(pos).longStream();
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsAt(@NotNull BlockPos pos) {
        return getAllGraphIdsAt(pos).mapToObj(graphs);
    }

    @Override
    public @Nullable BlockGraph getGraph(long id) {
        return graphs.get(id);
    }

    @Override
    public @NotNull LongStream getAllGraphIdsInChunkSection(@NotNull ChunkSectionPos pos) {
        SimpleBlockGraphChunk chunk = manager.getIfExists(pos);
        if (chunk == null) return LongStream.empty();

        return chunk.getGraphs().longStream();
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsInChunkSection(@NotNull ChunkSectionPos pos) {
        return getAllGraphIdsInChunkSection(pos).mapToObj(graphs);
    }

    @Override
    public @NotNull LongStream getAllGraphIdsInChunk(@NotNull ChunkPos pos) {
        SimpleBlockGraphPillar pillar = manager.getPillar(pos.x, pos.z);
        if (pillar == null) return LongStream.empty();

        LongSet graphsInChunk = new LongLinkedOpenHashSet();
        for (int chunkY = world.getBottomSectionCoord(); chunkY < world.getTopSectionCoord(); chunkY++) {
            SimpleBlockGraphChunk chunk = pillar.get(chunkY);
            if (chunk != null) {
                graphsInChunk.addAll(chunk.getGraphs());
            }
        }

        return graphsInChunk.longStream();
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsInChunk(@NotNull ChunkPos pos) {
        return getAllGraphIdsInChunk(pos).mapToObj(graphs);
    }

    @Override
    public @NotNull LongStream getAllGraphIds() {
        return graphs.keySet().longStream();
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphs() {
        return graphs.values().stream().map(Function.identity());
    }

    private void onUnload(SimpleBlockGraphPillar pillar) {
        // dedupe graphs
        LongSet unloading = new LongLinkedOpenHashSet();
        for (int chunkY = world.getBottomSectionCoord(); chunkY < world.getTopSectionCoord(); chunkY++) {
            SimpleBlockGraphChunk chunk = pillar.get(chunkY);
            if (chunk != null) {
                unloading.addAll(chunk.getGraphs());
            }
        }

        for (long graphId : unloading) {
            SimpleBlockGraph graph = graphs.get(graphId);
            if (graph != null) {
                graph.unloadInChunk(pillar.x, pillar.z);
            } else {
                GLLog.warn("Tried to unload graph that does not exist. Id: {}", graphId);
            }
        }
    }

    @Override
    public void markDirty(long graphId) {}

    @Override
    public @NotNull SimpleBlockGraph createGraph(boolean initializeGraphEntities) {
        throw new UnsupportedOperationException("Graphs should never be split on the client");
    }

    private @NotNull SimpleBlockGraph getOrCreateGraph(long graphId) {
        return graphs.computeIfAbsent(graphId, id -> new SimpleBlockGraph(this, id, false));
    }

    @Override
    public void destroyGraph(long id) {
        SimpleBlockGraph graph = graphs.get(id);
        if (graph == null) {
            GLLog.warn("Attempted to destroy graph that does not exist. Id: {}", id);
            return;
        }

        destroyGraphImpl(graph);
    }

    private void destroyGraphImpl(SimpleBlockGraph graph) {
        long id = graph.getId();

        graphs.remove(id);

        for (long sectionPos : graph.chunks) {
            SimpleBlockGraphChunk chunk = manager.getIfExists(ChunkSectionPos.from(sectionPos));
            if (chunk != null) {
                // Is called by graph.merge, which removes all nodes from the graph being deleted first.
                chunk.removeGraph(id);
            } else {
                GLLog.warn("Attempted to destroy graph in chunk that does not exist. Id: {}, chunk: {}", id,
                    ChunkSectionPos.from(sectionPos));
            }
        }

        graph.onDestroy();
    }

    @Override
    public void putGraphWithNode(long id, @NotNull NodePos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos.pos());
        SimpleBlockGraphChunk chunk = manager.getOrCreate(sectionPos);
        if (chunk != null) {
            chunk.putGraphWithNode(id, pos, graphs);
        } else {
            GLLog.warn("Attempted to add graph in chunk that is outside client range. Id: {}, chunk: {}, node: {}", id,
                sectionPos, pos);
        }
    }

    @Override
    public void removeGraphWithNode(long id, @NotNull NodePos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos.pos());
        SimpleBlockGraphChunk chunk = manager.getIfExists(sectionPos);
        if (chunk != null) {
            chunk.removeGraphWithNodeUnchecked(pos);
        } else {
            GLLog.warn("Tried to remove node from non-existent chunk. Id: {}, chunk: {}, node: {}", id, sectionPos,
                pos);
        }
    }

    @Override
    public void removeGraphInPos(long id, @NotNull BlockPos pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        SimpleBlockGraphChunk chunk = manager.getIfExists(sectionPos);
        if (chunk != null) {
            chunk.removeGraphInPosUnchecked(id, pos);
        } else {
            GLLog.warn("Tried to remove graph from non-existent chunk. Id: {}, chunk: {}, block: {}", id, sectionPos,
                pos);
        }
    }

    @Override
    public void removeGraphInChunk(long id, long pos) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(pos);
        SimpleBlockGraphChunk chunk = manager.getIfExists(sectionPos);
        if (chunk != null) {
            chunk.removeGraphUnchecked(id);
        } else {
            GLLog.warn("Tried to remove graph from non-existent chunk. Id: {}, chunk: {}", id, sectionPos);
        }
    }

    @Override
    public void removeGraphInPoses(long id, @NotNull Iterable<NodePos> nodes, @NotNull Iterable<BlockPos> poses,
                                   @NotNull LongIterable chunkPoses) {
        for (NodePos node : nodes) {
            removeGraphWithNode(id, node);
        }
        for (BlockPos pos : poses) {
            removeGraphInPos(id, pos);
        }
        for (long pos : chunkPoses) {
            removeGraphInChunk(id, pos);
        }
    }

    @Override
    public void scheduleCallbackUpdate(@NotNull NodeHolder<BlockNode> node, boolean validate) {}

    @Override
    public void graphUpdated(SimpleBlockGraph graph) {}

    @Override
    public void sendNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node) {}

    @Override
    public void sendMerge(BlockGraph from, BlockGraph into) {}

    @Override
    public void sendLink(BlockGraph graph, LinkHolder<LinkKey> link) {}

    @Override
    public void sendUnlink(BlockGraph graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {}

    @Override
    public void sendSplitInto(BlockGraph from, BlockGraph into) {}

    @Override
    public void sendNodeRemove(BlockGraph graph, NodeHolder<BlockNode> holder) {}
}
