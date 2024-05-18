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
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.impl.payload.ChunkDataPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.LinkPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.MergePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeAddPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeRemovePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadExternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadGraph;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadInternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadNode;
import com.kneelawk.graphlib.syncing.knet.impl.payload.SplitPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.UnlinkPayload;
import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

public final class KNetDecoding {
    private KNetDecoding() {}

    private static ClientGraphWorldImpl getWorld(KNetSyncedUniverse universe, String packetName)
        throws PayloadHandlingException {
        ClientGraphWorldImpl world = ((SyncedUniverseImpl) universe).getClientGraphView();
        if (world == null)
            throw new PayloadHandlingErrorException("Received " + packetName + " but client GraphWorld was null");

        return world;
    }

    public static void receiveChunkDataPacket(ChunkDataPayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "chunk data");

        ChunkPos chunkPos = payload.chunkPos();
        if (!world.tryCreateGraphPillar(chunkPos.x, chunkPos.z)) {
            GLLog.warn("Received pillar outside current client range at ({}, {})", chunkPos.x, chunkPos.z);
            payload.discard();
            return;
        }

        for (PayloadGraph payloadGraph : payload.graphs()) {
            long graphId = payloadGraph.graphId();
            BlockGraphImpl graph = world.getOrCreateGraph(graphId);

            // load graph entities if they exist
            graph.initializeGraphEntities(payloadGraph.entities());

            List<NodeHolder<BlockNode>> nodeList = new ObjectArrayList<>(payloadGraph.nodes().size());
            for (PayloadNode payloadNode : payloadGraph.nodes()) {
                // decode block node
                NodePos nodePos = payloadNode.nodePos();

                // decode node entity
                NodeEntity entity = payloadNode.entity().orElse(null);

                NodeHolder<BlockNode> holder = graph.createNode(nodePos.pos(), nodePos.node(), entity, false);
                nodeList.add(holder);
            }

            // decode internal links
            for (PayloadInternalLink payloadLink : payloadGraph.internalLinks()) {
                int nodeAIndex = payloadLink.firstIndex();
                int nodeBIndex = payloadLink.secondIndex();

                if (nodeAIndex < 0 || nodeAIndex >= nodeList.size()) {
                    GLLog.error("Received chunk packet @ {} with invalid links. Node index {} is invalid.",
                        payload.chunkPos(), nodeAIndex);
                    payloadLink.entity().ifPresent(LinkEntity::onDiscard);
                    continue;
                }

                if (nodeBIndex < 0 || nodeBIndex >= nodeList.size()) {
                    GLLog.error("Received chunk packet @ {} with invalid links. Node index {} is invalid.",
                        payload.chunkPos(), nodeBIndex);
                    payloadLink.entity().ifPresent(LinkEntity::onDiscard);
                    continue;
                }

                NodeHolder<BlockNode> nodeA = nodeList.get(nodeAIndex);
                NodeHolder<BlockNode> nodeB = nodeList.get(nodeBIndex);

                LinkKey linkKey = payloadLink.key();

                // read link entity
                LinkEntity entity = payloadLink.entity().orElse(null);

                graph.link(nodeA, nodeB, linkKey, entity, false);
            }

            // decode external links
            for (PayloadExternalLink payloadLink : payloadGraph.externalLinks()) {
                LinkPos link = payloadLink.linkPos();

                NodeHolder<BlockNode> holderA = graph.getNodeAt(link.first());
                NodeHolder<BlockNode> holderB = graph.getNodeAt(link.second());

                // read link entity
                LinkEntity entity = payloadLink.entity().orElse(null);

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
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "node add");

        PayloadNode payloadNode = payload.node();
        NodePos pos = payloadNode.nodePos();

        BlockPos blockPos = pos.pos();
        if (!world.isInRadius(new ChunkPos(blockPos))) {
            GLLog.warn("Received node add @ {} that is outside client chunk radius", pos);

            payloadNode.entity().ifPresent(NodeEntity::onDiscard);
            payload.graphEntities().forEach(GraphEntity::onDiscard);

            return;
        }

        NodeEntity entity = payloadNode.entity().orElse(null);

        BlockGraphImpl graph = world.getOrCreateGraph(payload.graphId());
        graph.initializeGraphEntities(payload.graphEntities());

        graph.createNode(blockPos, pos.node(), entity, true);
    }

    public static void receiveMerge(MergePayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        ClientGraphWorldImpl world = getWorld(payload.universe(), "merge");
        BlockGraphImpl from = world.getGraph(payload.fromId());
        if (from == null) {
            // we don't know the graph being merged from, so we can safely ignore this packet
            payload.intoGraphEntities().forEach(GraphEntity::onDiscard);
            return;
        }

        // however, it is possible for a graph we do know about to get merged into one we don't know about yet
        BlockGraphImpl into = world.getOrCreateGraph(payload.intoId());
        into.initializeGraphEntities(payload.intoGraphEntities());

        // do the merge
        into.merge(from);
    }

    public static void receiveLink(LinkPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "link");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received link in unknown graph {}", payload.graphId());

            payload.link().entity().ifPresent(LinkEntity::onDiscard);
            return;
        }

        PayloadExternalLink payloadLink = payload.link();

        LinkPos linkPos = payloadLink.linkPos();

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            payloadLink.entity().ifPresent(LinkEntity::onDiscard);
            return;
        }

        LinkEntity entity = payloadLink.entity().orElse(null);

        graph.link(nodeA, nodeB, linkPos.key(), entity, true);
    }

    public static void receiveUnlink(UnlinkPayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "unlink");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received unlink in unknown graph {}", payload.graphId());
            return;
        }

        LinkPos linkPos = payload.linkPos();

        NodeHolder<BlockNode> nodeA = graph.getNodeAt(linkPos.first());
        NodeHolder<BlockNode> nodeB = graph.getNodeAt(linkPos.second());
        if (nodeA == null || nodeB == null) {
            // unknown nodes means they're outside our range
            return;
        }

        graph.unlink(nodeA, nodeB, linkPos.key());
    }

    public static void receiveSplit(SplitPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "split");

        BlockGraphImpl from = world.getGraph(payload.fromId());
        if (from == null) {
            // we don't know the graph being split from, so we can safely ignore this packet
            payload.graphEntities().forEach(GraphEntity::onDiscard);
            return;
        }

        // however, the into graph is normally a newly created one
        BlockGraphImpl into = world.getOrCreateGraph(payload.intoId());
        into.initializeGraphEntities(payload.graphEntities());

        // load the nodes to be split off
        Set<NodePos> toMove = new ObjectLinkedOpenHashSet<>(payload.toMove());

        // Split Into only moves nodes from actually knows about, so nodes that are outside the client radius get
        // discarded.
        from.splitInto(into, toMove);
    }

    public static void receiveNodeRemove(NodeRemovePayload payload, PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        KNetSyncedUniverse universe = payload.universe();
        ClientGraphWorldImpl world = getWorld(universe, "node remove");

        BlockGraphImpl graph = world.getGraph(payload.graphId());
        if (graph == null) {
            GLLog.warn("Received node remove in unknown graph {}", payload.graphId());
            return;
        }

        NodePos pos = payload.nodePos();

        NodeHolder<BlockNode> node = graph.getNodeAt(pos);
        // ignore removals of nodes we don't know about
        if (node == null) return;

        graph.destroyNode(node, false);
    }
}
