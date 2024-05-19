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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.knet.api.SyncingKNet;
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
import com.kneelawk.knet.api.channel.NoContextPlayChannel;

public final class KNetEncoding {
    private KNetEncoding() {}

    @SuppressWarnings("unchecked")
    private static List<GraphEntity<?>> getGraphEntities(BlockGraphImpl graph) {
        return (List<GraphEntity<?>>) graph.getGraphView().getUniverse().getAllGraphEntityTypes().stream()
            .map(graph::getGraphEntity).toList();
    }

    private static <P extends CustomPacketPayload> void sendToFilteredWatching(NoContextPlayChannel<P> channel,
                                                                               P payload,
                                                                               ServerLevel world, BlockPos blockPos,
                                                                               SyncProfile sp) {
        Collection<ServerPlayer> watching =
            world.getChunkSource().chunkMap.getPlayers(new ChunkPos(blockPos), false);

        for (ServerPlayer player : watching) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                channel.send(player, payload);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendChunkData(ServerGraphWorldImpl world, ServerPlayer player, ChunkPos chunkPos) {
        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        // SyncProfile checking happens before this method is called

        // collect graphs to encode
        Long2ObjectMap<BlockGraphImpl> toEncode = new Long2ObjectLinkedOpenHashMap<>();
        for (int chunkY = world.getWorld().getMinSection();
             chunkY < world.getWorld().getMaxSection(); chunkY++) {
            PrimitiveIterator.OfLong graphIds =
                world.getAllGraphIdsInChunkSection(SectionPos.of(chunkPos, chunkY)).iterator();
            while (graphIds.hasNext()) {
                long graphId = graphIds.nextLong();
                BlockGraphImpl graph = world.getGraph(graphId);
                if (graph != null) {
                    toEncode.put(graphId, graph);
                }
            }
        }

        // Don't send anything if there's nothing to send
        if (toEncode.isEmpty()) return;

        // write graphs
        List<PayloadGraph> graphs = new ObjectArrayList<>(toEncode.size());
        for (BlockGraphImpl graph : toEncode.values()) {

            // collections for managing links
            Object2IntMap<NodePos> indexMap = new Object2IntOpenHashMap<>();
            indexMap.defaultReturnValue(-1);
            Set<LinkPos> internalLinks = new ObjectLinkedOpenHashSet<>();
            Set<LinkPos> externalLinks = new ObjectLinkedOpenHashSet<>();

            // iterate over only the nodes we want to synchronize
            CacheCategory<BlockNode> nodeFilter = (CacheCategory<BlockNode>) universe.getSyncProfile().getNodeFilter();
            Iterator<NodeHolder<BlockNode>> iter;
            if (nodeFilter != null) {
                iter = graph.getCachedNodes(nodeFilter).iterator();
            } else {
                iter = graph.getNodes().iterator();
            }

            // write nodes
            List<PayloadNode> nodes = new ObjectArrayList<>();
            while (iter.hasNext()) {
                NodeHolder<BlockNode> holder = iter.next();
                BlockPos blockPos = holder.getBlockPos();

                if (blockPos.getX() < chunkPos.getMinBlockX() || chunkPos.getMaxBlockX() < blockPos.getX() ||
                    blockPos.getZ() < chunkPos.getMinBlockZ() || chunkPos.getMaxBlockZ() < blockPos.getZ()) {
                    continue;
                }

                // put the node into the index map for links to look up
                indexMap.put(holder.getPos(), nodes.size());

                // collect the links
                for (LinkHolder<LinkKey> link : holder.getConnections()) {
                    NodeHolder<BlockNode> other = link.other(holder);

                    if (nodeFilter != null && !nodeFilter.matches(other)) continue;

                    BlockPos otherPos = other.getBlockPos();
                    if (otherPos.getX() < chunkPos.getMinBlockX() || chunkPos.getMaxBlockX() < otherPos.getX() ||
                        otherPos.getZ() < chunkPos.getMinBlockZ() || chunkPos.getMaxBlockZ() < otherPos.getZ()) {
                        externalLinks.add(link.getPos());
                    } else {
                        internalLinks.add(link.getPos());
                    }
                }

                nodes.add(new PayloadNode(holder.getPos(), Optional.ofNullable(holder.getNodeEntity())));
            }

            // write internal links
            List<PayloadInternalLink> iLinks = new ObjectArrayList<>(internalLinks.size());
            for (LinkPos link : internalLinks) {
                int nodeAIndex = indexMap.getInt(link.first());
                int nodeBIndex = indexMap.getInt(link.second());

                if (nodeAIndex < 0 || nodeBIndex < 0) {
                    GLLog.warn(
                        "Tried to send an internal link to a node that does not exist within the same chunk. Link: {}",
                        link);
                    continue;
                }

                iLinks.add(new PayloadInternalLink(nodeAIndex, nodeBIndex, link.key(),
                    Optional.ofNullable(graph.getLinkEntity(link))));
            }

            // write external
            List<PayloadExternalLink> eLinks = new ObjectArrayList<>(externalLinks.size());
            for (LinkPos link : externalLinks) {
                eLinks.add(new PayloadExternalLink(link, Optional.ofNullable(graph.getLinkEntity(link))));
            }

            graphs.add(new PayloadGraph(graph.getId(), getGraphEntities(graph), nodes, iLinks, eLinks));
        }

        KNetChannels.CHUNK_DATA.send(player,
            new ChunkDataPayload(universe, chunkPos, graphs));
    }

    public static void sendNodeAdd(BlockGraphImpl graph, NodeHolder<BlockNode> node) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendNodeAdd should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(node)) return;

        sendToFilteredWatching(KNetChannels.NODE_ADD, new NodeAddPayload(universe, graph.getId(),
                new PayloadNode(node.getPos(), Optional.ofNullable(node.getNodeEntity())), getGraphEntities(graph)),
            world.getWorld(), node.getBlockPos(), sp);
    }

    public static void sendMerge(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(into.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendMerge should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        MergePayload payload = new MergePayload(universe, from.getId(), into.getId(), getGraphEntities(into));

        Set<ServerPlayer> sendTo = new LinkedHashSet<>();
        for (var iter = into.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
                iter.next().chunk(), false));
        }
        for (var iter = from.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
                iter.next().chunk(), false));
        }

        for (ServerPlayer player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.MERGE.send(player, payload);
            }
        }
    }

    public static void sendLink(BlockGraphImpl graph, LinkHolder<LinkKey> link) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendLink should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(link.getFirst()) && nodeFilter.matches(link.getSecond())))
            return;
        LinkPayload payload = new LinkPayload(universe, graph.getId(),
            new PayloadExternalLink(link.getPos(), Optional.ofNullable(link.getLinkEntity())));

        Set<ServerPlayer> sendTo = new LinkedHashSet<>();
        sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
            new ChunkPos(link.getFirstBlockPos()), false));
        sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
            new ChunkPos(link.getSecondBlockPos()), false));

        for (ServerPlayer player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.LINK.send(player, payload);
            }
        }
    }

    public static void sendUnlink(BlockGraphImpl graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendUnlink should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(a) && nodeFilter.matches(b))) return;

        UnlinkPayload payload = new UnlinkPayload(universe, graph.getId(), new LinkPos(a.getPos(), b.getPos(), key));

        Set<ServerPlayer> sendTo = new LinkedHashSet<>();
        sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
            new ChunkPos(a.getBlockPos()), false));
        sendTo.addAll(world.getWorld().getChunkSource().chunkMap.getPlayers(
            new ChunkPos(b.getBlockPos()), false));

        for (ServerPlayer player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.UNLINK.send(player, payload);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendSplit(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(from.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendSplit should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        List<NodePos> toMove;
        CacheCategory<BlockNode> nodeFilter = (CacheCategory<BlockNode>) universe.getSyncProfile().getNodeFilter();
        if (nodeFilter != null) {
            toMove = into.getCachedNodes(nodeFilter).stream().map(NodeHolder::getPos).toList();
        } else {
            toMove = into.getNodes().map(NodeHolder::getPos).toList();
        }

        SplitPayload payload = new SplitPayload(universe, from.getId(), into.getId(), getGraphEntities(into), toMove);

        Set<ServerPlayer> sendTo = new LinkedHashSet<>();
        for (var iter1 = into.getChunks().iterator(); iter1.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkSource().chunkMap.getPlayers(iter1.next().chunk(), false));
        }
        for (var iter1 = from.getChunks().iterator(); iter1.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkSource().chunkMap.getPlayers(iter1.next().chunk(), false));
        }

        for (ServerPlayer player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.SPLIT.send(player, payload);
            }
        }
    }

    public static void sendNodeRemove(BlockGraphImpl graph, NodeHolder<BlockNode> holder) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendNodeRemove should only be called on the logical server");

        KNetSyncedUniverse universe = SyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(holder)) return;

        NodeRemovePayload payload = new NodeRemovePayload(universe, graph.getId(), holder.getPos());

        sendToFilteredWatching(KNetChannels.NODE_REMOVE, payload, world.getWorld(), holder.getBlockPos(), sp);
    }
}
