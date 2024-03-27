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

package com.kneelawk.graphlib.syncing.knet.impl;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

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
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.ChunkDataPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.LinkPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.MergePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeAddPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeRemovePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadExternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadGraph;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadHeader;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadInternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadNode;
import com.kneelawk.graphlib.syncing.knet.impl.payload.SplitPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.UnlinkPayload;
import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;

public final class KNetDecoding {
    private KNetDecoding() {}

    private static ClientGraphWorldImpl getWorld(Identifier universeId, String packetName)
        throws PayloadHandlingException {
        SyncedUniverseImpl universe = GraphLibSyncingImpl.SYNCED_UNIVERSE.get(universeId);
        if (universe == null)
            throw new PayloadHandlingErrorException("Received " + packetName + " for unknown universe");

        ClientGraphWorldImpl world = universe.getClientGraphView();
        if (world == null)
            throw new PayloadHandlingErrorException("Received " + packetName + " but client GraphWorld was null");

        return world;
    }

    private static <T> T getType(int typeIdInt, Palette<Identifier> palette, Function<Identifier, T> getter,
                                 String name, Object position) throws PayloadHandlingException {
        Identifier typeId = palette.get(typeIdInt);
        if (typeId == null) throw new PayloadHandlingErrorException(
            "Unable to decode " + name + " type id int as id. Int: " + typeIdInt + " @ " + position);

        T type = getter.apply(typeId);
        if (type == null)
            throw new PayloadHandlingErrorException(
                "Received unknown " + name + " type id: " + typeId + " @ " + position);

        return type;
    }

    private static void loadGraphEntities(BlockGraphImpl graph, int[] graphEntityIds, NetByteBuf data,
                                          Palette<Identifier> palette, KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        List<GraphEntity<?>> decodedEntities = new ObjectArrayList<>();

        for (int typeIdInt : graphEntityIds) {
            GraphEntityType<?> type =
                getType(typeIdInt, palette, universe.getUniverse()::getGraphEntityType, "graph entity", graph.getId());

            GraphEntitySyncing<?> syncing = universe.getGraphEntitySyncing(type);

            GraphEntity<?> entity = syncing.decode(data);
            decodedEntities.add(entity);
        }

        graph.initializeGraphEntities(decodedEntities);
    }

    private static @Nullable NodeEntity readNodeEntity(OptionalInt entityId, NetByteBuf data,
                                                       Palette<Identifier> palette, KNetSyncedUniverse universe,
                                                       NodePos pos) throws PayloadHandlingException {
        if (entityId.isPresent()) {
            NodeEntityType entityType =
                getType(entityId.getAsInt(), palette, universe.getUniverse()::getNodeEntityType, "node entity", pos);
            return universe.getNodeEntitySyncing(entityType).decode(data);
        }
        return null;
    }

    private static @Nullable LinkEntity readLinkEntity(OptionalInt entityId, NetByteBuf data,
                                                       Palette<Identifier> palette, KNetSyncedUniverse universe,
                                                       LinkPos pos) throws PayloadHandlingException {
        if (entityId.isPresent()) {
            LinkEntityType entityType =
                getType(entityId.getAsInt(), palette, universe.getUniverse()::getLinkEntityType, "link entity", pos);
            return universe.getLinkEntitySyncing(entityType).decode(data);
        }
        return null;
    }

    public static void receiveChunkDataPacket(ChunkDataPayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        PayloadHeader header = payload.header();
        Palette<Identifier> palette = header.palette();
        NetByteBuf data = header.data();

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(header.universeId());
        ClientGraphWorldImpl world = getWorld(header.universeId(), "chunk data");

        ChunkPos chunkPos = payload.chunkPos();
        if (!world.tryCreateGraphPillar(chunkPos.x, chunkPos.z)) {
            GLLog.warn("Received pillar outside current client range at ({}, {})", chunkPos.x, chunkPos.z);
            return;
        }

        for (PayloadGraph payloadGraph : payload.graphs()) {
            long graphId = payloadGraph.graphId();
            BlockGraphImpl graph = world.getOrCreateGraph(graphId);

            // load graph entities if they exist
            loadGraphEntities(graph, payloadGraph.graphEntityIds(), data, palette, universe);

            List<NodeHolder<BlockNode>> nodeList = new ObjectArrayList<>(payloadGraph.nodes().size());
            for (PayloadNode payloadNode : payloadGraph.nodes()) {
                // decode block node
                NodePos nodePos =
                    GraphLibSyncingKNet.decodeNodePosSmall(payloadNode.nodePos(), data, palette, universe);

                // decode node entity
                NodeEntity entity = readNodeEntity(payloadNode.entityTypeId(), data, palette, universe, nodePos);

                NodeHolder<BlockNode> holder = graph.createNode(nodePos.pos(), nodePos.node(), entity, false);
                nodeList.add(holder);
            }

            // decode internal links
            for (PayloadInternalLink payloadLink : payloadGraph.internalLinks()) {
                int nodeAIndex = payloadLink.firstIndex();
                int nodeBIndex = payloadLink.secondIndex();

                if (nodeAIndex < 0 || nodeAIndex >= nodeList.size()) {
                    // packet is foo bar
                    throw new PayloadHandlingErrorException(
                        "Received packet with invalid links. Node index " + nodeAIndex + " is invalid.");
                }

                if (nodeBIndex < 0 || nodeBIndex >= nodeList.size()) {
                    // packet is foo bar
                    throw new PayloadHandlingErrorException(
                        "Received packet with invalid links. Node index " + nodeBIndex + " is invalid.");
                }

                NodeHolder<BlockNode> nodeA = nodeList.get(nodeAIndex);
                NodeHolder<BlockNode> nodeB = nodeList.get(nodeBIndex);

                LinkKeyType linkType =
                    getType(payloadLink.keyTypeId(), palette, universe.getUniverse()::getLinkKeyType, "link key",
                        nodeA.getPos() + "-" + nodeB.getPos());

                LinkKey linkKey = universe.getLinkKeySyncing(linkType).decode(data);

                // read link entity
                LinkEntity entity = readLinkEntity(payloadLink.entityTypeId(), data, palette, universe,
                    new LinkPos(nodeA.getPos(), nodeB.getPos(), linkKey));

                graph.link(nodeA, nodeB, linkKey, entity, false);
            }

            // decode external links
            for (PayloadExternalLink payloadLink : payloadGraph.externalLinks()) {
                LinkPos link =
                    GraphLibSyncingKNet.decodeLinkPosSmall(payloadLink.linkPos(), data, data, palette, universe);

                NodeHolder<BlockNode> holderA = graph.getNodeAt(link.first());
                NodeHolder<BlockNode> holderB = graph.getNodeAt(link.second());

                // read link entity
                LinkEntity entity = readLinkEntity(payloadLink.entityTypeId(), data, palette, universe, link);

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
        }
    }

    public static void receiveNodeAdd(NodeAddPayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        PayloadHeader header = payload.header();
        Palette<Identifier> palette = header.palette();
        NetByteBuf data = header.data();

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(header.universeId());
        ClientGraphWorldImpl world = getWorld(header.universeId(), "node add");

        PayloadNode payloadNode = payload.node();
        NodePos pos = GraphLibSyncingKNet.decodeNodePosSmall(payloadNode.nodePos(), data, palette, universe);

        BlockPos blockPos = pos.pos();
        if (!world.isInRadius(new ChunkPos(blockPos))) {
            GLLog.warn("Received node add @ {} that is outside client chunk radius", pos);
            return;
        }

        BlockGraphImpl graph = world.getOrCreateGraph(payload.graphId());
        loadGraphEntities(graph, payload.graphEntityIds(), data, palette, universe);

        NodeEntity entity = readNodeEntity(payloadNode.entityTypeId(), data, palette, universe, pos);

        graph.createNode(blockPos, pos.node(), entity, true);
    }

    public static void receiveMerge(MergePayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        PayloadHeader header = payload.header();
        Palette<Identifier> palette = header.palette();
        NetByteBuf data = header.data();

        ClientGraphWorldImpl world = getWorld(header.universeId(), "merge");
        BlockGraphImpl from = world.getGraph(payload.fromId());
        if (from == null) {
            // we don't know the graph being merged from, so we can safely ignore this packet
            return;
        }

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(header.universeId());

        // however, it is possible for a graph we do know about to get merged into one we don't know about yet
        BlockGraphImpl into = world.getOrCreateGraph(payload.intoId());
        loadGraphEntities(into, payload.intoGraphEntityIds(), data, palette, universe);

        // do the merge
        into.merge(from);
    }

    public static void receiveLink(LinkPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        PayloadHeader header = payload.header();
        Palette<Identifier> palette = header.palette();
        NetByteBuf data = header.data();

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(header.universeId());
        ClientGraphWorldImpl world = getWorld(header.universeId(), "link");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received link in unknown graph {}", payload.graphId());
            return;
        }

        PayloadExternalLink payloadLink = payload.link();

        LinkPos linkPos = GraphLibSyncingKNet.decodeLinkPosSmall(payloadLink.linkPos(), data, data, palette, universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            return;
        }

        LinkEntity entity = readLinkEntity(payloadLink.entityTypeId(), data, palette, universe, linkPos);

        graph.link(nodeA, nodeB, linkPos.key(), entity, true);
    }

    public static void receiveUnlink(UnlinkPayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(payload.universeId());
        ClientGraphWorldImpl world = getWorld(payload.universeId(), "unlink");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received unlink in unknown graph {}", payload.graphId());
            return;
        }

        LinkPos linkPos = GraphLibSyncingKNet.decodeLinkPos(payload.linkPos(), universe);

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            return;
        }

        graph.unlink(nodeA, nodeB, linkPos.key());
    }

    public static void receiveSplit(SplitPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        PayloadHeader header = payload.header();
        Palette<Identifier> palette = header.palette();
        NetByteBuf data = header.data();

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(header.universeId());
        ClientGraphWorldImpl world = getWorld(header.universeId(), "split");

        BlockGraphImpl from = world.getGraph(payload.fromId());
        if (from == null) {
            // we don't know the graph being split from, so we can safely ignore this packet
            return;
        }

        // however, the into graph is normally a newly created one
        BlockGraphImpl into = world.getOrCreateGraph(payload.intoId());
        loadGraphEntities(into, payload.graphEntityIds(), data, palette, universe);

        // load the nodes to be split off
        Set<NodePos> toMove = new ObjectLinkedOpenHashSet<>();
        for (NodePosSmallPayload nodePayload : payload.toMove()) {
            toMove.add(GraphLibSyncingKNet.decodeNodePosSmall(nodePayload, data, palette, universe));
        }

        // Split Into only moves nodes from actually knows about, so nodes that are outside the client radius get
        // discarded.
        from.splitInto(into, toMove);
    }

    public static void receiveNodeRemove(NodeRemovePayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(payload.universeId());
        ClientGraphWorldImpl world = getWorld(payload.universeId(), "node remove");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received node remove in unknown graph {}", payload.graphId());
            return;
        }

        NodePos pos = GraphLibSyncingKNet.decodeNodePos(payload.nodePos(), universe);

        NodeHolder<BlockNode> node = graph.getNodeAt(pos);
        // ignore removals of nodes we don't know about
        if (node == null) return;

        graph.destroyNode(node, false);
    }
}
