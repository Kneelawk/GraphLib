/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.impl;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.lns.api.graph.LNSSyncedUniverse;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.lns.api.util.PacketEncodingUtil;

public final class LNSDecoding {
    private LNSDecoding() {}

    public static void readChunkPillar(ClientGraphWorldImpl world, int chunkX, int chunkZ, NetByteBuf buf,
                                       IMsgReadCtx ctx) throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        if (!world.tryCreateGraphPillar(chunkX, chunkZ)) {
            GLLog.warn("Received pillar outside current client range at ({}, {})", chunkX, chunkZ);
            ctx.drop("Pillar outside range");
            return;
        }

        int graphCount = buf.readVarUnsignedInt();
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++) {
            buf.readMarker("gs");

            long graphId = buf.readVarUnsignedLong();
            BlockGraphImpl graph = world.getOrCreateGraph(graphId);

            // load graph entities if any exist
            loadGraphEntitiesFromPacket(graph, buf, ctx);

            List<NodeHolder<BlockNode>> nodeList = new ObjectArrayList<>();

            buf.readMarker("n");

            int nodeCount = buf.readInt();
            for (int i = 0; i < nodeCount; i++) {
                // decode block node
                NodePos nodePos = PacketEncodingUtil.decodeNodePos(buf, ctx, universe);

                BlockPos blockPos = nodePos.pos();

                // decode node entity
                NodeEntity entity = readNodeEntity(ctx, buf, blockPos, universe);

                NodeHolder<BlockNode> holder = graph.createNode(blockPos, nodePos.node(), entity, false);
                nodeList.add(holder);
            }

            buf.readMarker("il");

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
                    LNSNetworking.readType(buf, ctx.getConnection(), universe.getUniverse()::getLinkKeyType, "LinkKey",
                        nodeA.getBlockPos());

                if (!universe.hasLinkKeySyncing(linkType)) {
                    GLLog.error("Unable to decode LinkKey {} @ {}-{} because it has no packet decoder",
                        linkType.getId(), nodeA.getBlockPos(), nodeB.getBlockPos());
                    throw new InvalidInputDataException(
                        "Unable to decode LinkKey " + linkType.getId() + " @ " + nodeA.getBlockPos() + "-" +
                            nodeB.getBlockPos() + " because it has no packet decoder");
                }
                LinkKeySyncing linkDecoder = universe.getLinkKeySyncing(linkType);

                LinkKey linkKey = linkDecoder.decode(buf, ctx);

                // decode link entity
                LinkEntity entity =
                    readLinkEntity(buf, ctx, new LinkPos(nodeA.getPos(), nodeB.getPos(), linkKey), universe);

                graph.link(nodeA, nodeB, linkKey, entity, false);
            }

            buf.readMarker("el");

            // decode external links
            int eLinkCount = buf.readVarUnsignedInt();
            for (int i = 0; i < eLinkCount; i++) {
                LinkPos link = PacketEncodingUtil.decodeLinkPos(buf, ctx, universe);

                NodeHolder<BlockNode> holderA = graph.getNodeAt(link.first());
                NodeHolder<BlockNode> holderB = graph.getNodeAt(link.second());

                // read link entity
                LinkEntity entity = readLinkEntity(buf, ctx, link, universe);

                if (holderA != null && holderB != null) {
                    // ignore links with missing nodes,
                    // they'll just happen sometimes because the server will send links to nodes we don't know about
                    graph.link(holderA, holderB, link.key(), entity, false);
                } else {
                    if (entity != null) {
                        // We don't actually need this link, so we're discarding its entity.
                        entity.onDiscard();
                    }
                }
            }

            buf.readMarker("ge");
        }
    }

    private static void loadGraphEntitiesFromPacket(BlockGraphImpl graph, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(graph.getGraphView());

        List<GraphEntity<?>> decodedEntities = new ObjectArrayList<>();

        int entityCount = buf.readVarUnsignedInt();
        for (int entityIndex = 0; entityIndex < entityCount; entityIndex++) {
            int typeIdInt = buf.readVarUnsignedInt();
            Identifier typeId = LNSNetworking.ID_CACHE.getObj(ctx.getConnection(), typeIdInt);
            if (typeId == null) {
                GLLog.warn("Unable to decode graph entity type id int as id. Int: {}", typeIdInt);
                throw new InvalidInputDataException(
                    "Unable to decode graph entity type id int as id. Int: " + typeIdInt);
            }

            GraphEntityType<?> type = universe.getUniverse().getGraphEntityType(typeId);
            if (type == null) {
                GLLog.warn("Received unknown graph entity type id: {}", typeId);
                throw new InvalidInputDataException("Received unknown graph entity type id: " + typeId);
            }

            if (!universe.hasGraphEntitySyncing(type)) {
                GLLog.warn("Received graph entity but type has no packet decoder. Id: {}", typeId);
                throw new InvalidInputDataException(
                    "Received graph entity but type has no packet decoder. Id: " + typeId);
            }
            GraphEntitySyncing<?> decoder = universe.getGraphEntitySyncing(type);

            GraphEntity<?> entity = decoder.decode(buf, ctx);
            decodedEntities.add(entity);
        }

        graph.initializeGraphEntities(decodedEntities);
    }

    private static @Nullable NodeEntity readNodeEntity(IMsgReadCtx ctx, NetByteBuf buf, BlockPos blockPos,
                                                       LNSSyncedUniverse universe) throws InvalidInputDataException {
        NodeEntity entity = null;
        if (buf.readBoolean()) {
            NodeEntityType entityType =
                LNSNetworking.readType(buf, ctx.getConnection(), universe.getUniverse()::getNodeEntityType,
                    "NodeEntity", blockPos);
            if (!universe.hasNodeEntitySyncing(entityType)) {
                GLLog.error("Unable to decode NodeEntity {} @ {} because it has no packet decoder", entityType.getId(),
                    blockPos);
                throw new InvalidInputDataException(
                    "Unable to decode NodeEntity " + entityType.getId() + " @ " + blockPos +
                        " because it has no packet decoder");
            }
            NodeEntitySyncing entityDecoder = universe.getNodeEntitySyncing(entityType);

            entity = entityDecoder.decode(buf, ctx);
        }
        return entity;
    }

    private static @Nullable LinkEntity readLinkEntity(NetByteBuf buf, IMsgReadCtx ctx, LinkPos link,
                                                       LNSSyncedUniverse universe) throws InvalidInputDataException {
        LinkEntity entity = null;
        if (buf.readBoolean()) {
            LinkEntityType entityType =
                LNSNetworking.readType(buf, ctx.getConnection(), universe.getUniverse()::getLinkEntityType,
                    "LinkEntity", link.first().pos());
            if (!universe.hasLinkEntitySyncing(entityType)) {
                GLLog.error("Unable to decode LinkEntity {} @ {} because it has no packet decoder", entityType.getId(),
                    link);
                throw new InvalidInputDataException("Unable to decode LinkEntity " + entityType.getId() + " @ " + link +
                    " because it has no packet decoder");
            }
            LinkEntitySyncing entityDecoder = universe.getLinkEntitySyncing(entityType);

            entity = entityDecoder.decode(buf, ctx);
        }
        return entity;
    }

    public static void readNodeAdd(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        NodePos pos = PacketEncodingUtil.decodeNodePos(buf, ctx, universe);

        BlockNode node = pos.node();
        BlockPos blockPos = pos.pos();

        ChunkPos chunkPos = new ChunkPos(pos.pos());
        if (!world.isInRadius(chunkPos)) {
            GLLog.warn("Received node add @ {} that is outside client chunk radius", pos);
            ctx.drop("Received node add outside client chunk radius");
            return;
        }

        long graphId = buf.readVarUnsignedLong();

        BlockGraphImpl graph = world.getOrCreateGraph(graphId);

        loadGraphEntitiesFromPacket(graph, buf, ctx);

        // decode node entity
        NodeEntity entity = readNodeEntity(ctx, buf, blockPos, universe);

        graph.createNode(blockPos, node, entity, true);
    }

    public static void readMerge(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        long fromId = buf.readVarUnsignedLong();
        BlockGraphImpl from = world.getGraph(fromId);
        if (from == null) {
            // we don't know the graph being merged from, so we can safely ignore this packet
            ctx.drop("Unknown from graph");
            return;
        }

        long intoId = buf.readVarUnsignedLong();
        // however, it is possible for a graph we do know about to get merged into one we don't know about yet
        BlockGraphImpl into = world.getOrCreateGraph(intoId);

        // initialize into's graph entities if we haven't already
        loadGraphEntitiesFromPacket(into, buf, ctx);

        // do the merge
        into.merge(from);
    }

    public static void readLink(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        long graphId = buf.readVarUnsignedLong();
        BlockGraphImpl graph = world.getGraph(graphId);
        if (graph == null) {
            GLLog.warn("Received link in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        LinkPos linkPos = PacketEncodingUtil.decodeLinkPos(buf, ctx, universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            ctx.drop("Link outside range");
            return;
        }

        LinkEntity entity = readLinkEntity(buf, ctx, linkPos, universe);

        graph.link(nodeA, nodeB, linkPos.key(), entity, true);
    }

    public static void readUnlink(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        long graphId = buf.readVarUnsignedLong();
        BlockGraphImpl graph = world.getGraph(graphId);
        if (graph == null) {
            GLLog.warn("Received unlink in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        LinkPos linkPos = PacketEncodingUtil.decodeLinkPos(buf, ctx, universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            ctx.drop("Link outside range");
            return;
        }

        graph.unlink(nodeA, nodeB, linkPos.key());
    }

    public static void readSplitInto(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        long fromId = buf.readVarUnsignedLong();
        BlockGraphImpl from = world.getGraph(fromId);
        if (from == null) {
            // we don't know the graph being split from, so we can safely ignore this packet
            ctx.drop("Unknown from graph");
            return;
        }

        long intoId = buf.readVarUnsignedLong();
        // however, the into graph is normally a newly created one
        BlockGraphImpl into = world.getOrCreateGraph(intoId);

        // initialize into's graph entities
        loadGraphEntitiesFromPacket(into, buf, ctx);

        // load the nodes to be split off
        Set<NodePos> toSplit = new ObjectLinkedOpenHashSet<>();
        int nodeCount = buf.readVarUnsignedInt();
        for (int i = 0; i < nodeCount; i++) {
            NodePos pos = PacketEncodingUtil.decodeNodePos(buf, ctx, universe);

            toSplit.add(pos);
        }

        // Split Into only moves nodes from actually knows about, so nodes that are outside the client radius get
        // discarded.
        from.splitInto(into, toSplit);
    }

    public static void readNodeRemove(ClientGraphWorldImpl world, NetByteBuf buf, IMsgReadCtx ctx)
        throws InvalidInputDataException {
        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);

        long graphId = buf.readVarUnsignedLong();
        BlockGraphImpl graph = world.getGraph(graphId);
        if (graph == null) {
            GLLog.warn("Received node remove in unknown graph {}", graphId);
            ctx.drop("Unknown graph");
            return;
        }

        NodePos pos = PacketEncodingUtil.decodeNodePos(buf, ctx, universe);

        NodeHolder<BlockNode> node = graph.getNodeAt(pos);
        // ignore removals of nodes we don't know about
        if (node == null) return;

        graph.destroyNode(node, false);
    }
}
