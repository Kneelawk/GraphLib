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
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosPayload;
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
import com.kneelawk.knet.api.channel.NetPayload;
import com.kneelawk.knet.api.channel.NoContextChannel;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;

public final class KNetEncoding {
    private KNetEncoding() {}

    private static int[] writeGraphEntities(BlockGraphImpl graph, NetByteBuf data, Palette<Identifier> palette,
                                            KNetSyncedUniverse universe) {
        Collection<GraphEntityType<?>> types = graph.getGraphView().getUniverse().getAllGraphEntityTypes();
        int[] typeIds = new int[types.size()];
        int i = 0;
        for (GraphEntityType<?> type : types) {
            typeIds[i] = palette.keyFor(type.getId());
            universe.getGraphEntitySyncing(type).encode(graph.getGraphEntity(type), data);
        }
        return typeIds;
    }

    private static OptionalInt writeNodeEntity(NodeHolder<BlockNode> holder, BlockGraph graph,
                                               NetByteBuf data, Palette<Identifier> palette,
                                               KNetSyncedUniverse universe) {
        NodeEntity entity = graph.getNodeEntity(holder.getPos());
        if (entity != null) {
            NodeEntityType type = entity.getType();
            universe.getNodeEntitySyncing(type).encode(entity, data);
            return OptionalInt.of(palette.keyFor(type.getId()));
        } else {
            return OptionalInt.empty();
        }
    }

    private static OptionalInt writeLinkEntity(LinkPos link, BlockGraph graph, NetByteBuf data,
                                               Palette<Identifier> palette, KNetSyncedUniverse universe) {
        LinkEntity entity = graph.getLinkEntity(link);
        if (entity != null) {
            LinkEntityType type = entity.getType();
            universe.getLinkEntitySyncing(type).encode(entity, data);
            return OptionalInt.of(palette.keyFor(type.getId()));
        } else {
            return OptionalInt.empty();
        }
    }

    private static <P extends NetPayload> void sendToFilteredWatching(NoContextChannel<P> channel, P payload,
                                                                      ServerWorld world, BlockPos blockPos,
                                                                      SyncProfile sp) {
        Collection<ServerPlayerEntity> watching =
            world.getChunkManager().delegate.getPlayersWatchingChunk(new ChunkPos(blockPos), false);

        for (ServerPlayerEntity player : watching) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                channel.sendPlay(player, payload);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendChunkData(ServerGraphWorldImpl world, ServerPlayerEntity player, ChunkPos chunkPos) {
        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        // SyncProfile checking happens before this method is called

        Palette<Identifier> palette = new Palette<>();
        NetByteBuf data = NetByteBuf.buffer();

        // collect graphs to encode
        Long2ObjectMap<BlockGraphImpl> toEncode = new Long2ObjectLinkedOpenHashMap<>();
        for (int chunkY = world.getWorld().getBottomSectionCoord();
             chunkY < world.getWorld().getTopSectionCoord(); chunkY++) {
            PrimitiveIterator.OfLong graphIds =
                world.getAllGraphIdsInChunkSection(ChunkSectionPos.from(chunkPos, chunkY)).iterator();
            while (graphIds.hasNext()) {
                long graphId = graphIds.nextLong();
                BlockGraphImpl graph = world.getGraph(graphId);
                if (graph != null) {
                    toEncode.put(graphId, graph);
                }
            }
        }

        // write graphs
        List<PayloadGraph> graphs = new ObjectArrayList<>(toEncode.size());
        for (BlockGraphImpl graph : toEncode.values()) {
            // write graph entities if any exist
            int[] graphEntityIds = writeGraphEntities(graph, data, palette, universe);

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

                if (blockPos.getX() < chunkPos.getStartX() || chunkPos.getEndX() < blockPos.getX() ||
                    blockPos.getZ() < chunkPos.getStartZ() || chunkPos.getEndZ() < blockPos.getZ()) {
                    continue;
                }

                NodePosSmallPayload nodePosPayload =
                    GraphLibSyncingKNet.encodeNodePosSmall(holder.getPos(), data, palette, universe);

                OptionalInt entityId = writeNodeEntity(holder, graph, data, palette, universe);

                // put the node into the index map for links to look up
                indexMap.put(holder.getPos(), nodes.size());

                // collect the links
                for (LinkHolder<LinkKey> link : holder.getConnections()) {
                    NodeHolder<BlockNode> other = link.other(holder);

                    if (nodeFilter != null && !nodeFilter.matches(other)) continue;

                    BlockPos otherPos = other.getBlockPos();
                    if (otherPos.getX() < chunkPos.getStartX() || chunkPos.getEndX() < otherPos.getX() ||
                        otherPos.getZ() < chunkPos.getStartZ() || chunkPos.getEndZ() < otherPos.getZ()) {
                        externalLinks.add(link.getPos());
                    } else {
                        internalLinks.add(link.getPos());
                    }
                }

                nodes.add(new PayloadNode(nodePosPayload, entityId));
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

                LinkKey linkKey = link.key();
                LinkKeyType linkKeyType = linkKey.getType();
                int keyTypeId = palette.keyFor(linkKeyType.getId());
                universe.getLinkKeySyncing(linkKeyType).encode(linkKey, data);

                OptionalInt entityId = writeLinkEntity(link, graph, data, palette, universe);

                iLinks.add(new PayloadInternalLink(nodeAIndex, nodeBIndex, keyTypeId, entityId));
            }

            // write external
            List<PayloadExternalLink> eLinks = new ObjectArrayList<>(externalLinks.size());
            for (LinkPos link : externalLinks) {
                LinkPosSmallPayload linkPos =
                    GraphLibSyncingKNet.encodeLinkPosSmall(link, data, data, palette, universe);

                OptionalInt entityId = writeLinkEntity(link, graph, data, palette, universe);

                eLinks.add(new PayloadExternalLink(linkPos, entityId));
            }

            graphs.add(new PayloadGraph(graph.getId(), graphEntityIds, nodes, iLinks, eLinks));
        }

        KNetChannels.CHUNK_DATA.sendPlay(player,
            new ChunkDataPayload(new PayloadHeader(universe.getId(), palette, data), chunkPos, graphs));
    }

    public static void sendNodeAdd(BlockGraphImpl graph, NodeHolder<BlockNode> node) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendNodeAdd should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(node)) return;

        Palette<Identifier> palette = new Palette<>();
        NetByteBuf data = NetByteBuf.buffer();

        int[] graphEntityIds = writeGraphEntities(graph, data, palette, universe);
        NodePosSmallPayload nodePos = GraphLibSyncingKNet.encodeNodePosSmall(node.getPos(), data, palette, universe);
        OptionalInt nodeEntityId = writeNodeEntity(node, graph, data, palette, universe);

        sendToFilteredWatching(KNetChannels.NODE_ADD,
            new NodeAddPayload(new PayloadHeader(universe.getId(), palette, data), graph.getId(), graphEntityIds,
                new PayloadNode(nodePos, nodeEntityId)), world.getWorld(), node.getBlockPos(), sp);
    }

    public static void sendMerge(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(into.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendMerge should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        Palette<Identifier> palette = new Palette<>();
        NetByteBuf data = NetByteBuf.buffer();

        int[] intoGraphEntityIds = writeGraphEntities(into, data, palette, universe);
        MergePayload payload =
            new MergePayload(new PayloadHeader(universe.getId(), palette, data), from.getId(), into.getId(),
                intoGraphEntityIds);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        for (var iter = into.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(iter.next().toChunkPos(), false));
        }
        for (var iter = from.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(iter.next().toChunkPos(), false));
        }

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.MERGE.sendPlay(player, payload);
            }
        }
    }

    public static void sendLink(BlockGraphImpl graph, LinkHolder<LinkKey> link) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendLink should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(link.getFirst()) && nodeFilter.matches(link.getSecond())))
            return;

        Palette<Identifier> palette = new Palette<>();
        NetByteBuf data = NetByteBuf.buffer();

        LinkPosSmallPayload linkPos =
            GraphLibSyncingKNet.encodeLinkPosSmall(link.getPos(), data, data, palette, universe);
        OptionalInt entityId = writeLinkEntity(link.getPos(), graph, data, palette, universe);
        LinkPayload payload = new LinkPayload(new PayloadHeader(universe.getId(), palette, data), graph.getId(),
            new PayloadExternalLink(linkPos, entityId));

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        sendTo.addAll(
            world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(new ChunkPos(link.getFirstBlockPos()),
                false));
        sendTo.addAll(
            world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(new ChunkPos(link.getSecondBlockPos()),
                false));

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.LINK.sendPlay(player, payload);
            }
        }
    }

    public static void sendUnlink(BlockGraphImpl graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendUnlink should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(a) && nodeFilter.matches(b))) return;

        LinkPosPayload linkPos = GraphLibSyncingKNet.encodeLinkPos(new LinkPos(a.getPos(), b.getPos(), key), universe);
        UnlinkPayload payload = new UnlinkPayload(universe.getId(), graph.getId(), linkPos);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        sendTo.addAll(
            world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(new ChunkPos(a.getBlockPos()), false));
        sendTo.addAll(
            world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(new ChunkPos(b.getBlockPos()), false));

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.UNLINK.sendPlay(player, payload);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendSplit(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(from.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendSplit should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        Palette<Identifier> palette = new Palette<>();
        NetByteBuf data = NetByteBuf.buffer();

        int[] graphEntityIds = writeGraphEntities(into, data, palette, universe);

        Iterator<NodeHolder<BlockNode>> iter;
        CacheCategory<BlockNode> nodeFilter = (CacheCategory<BlockNode>) universe.getSyncProfile().getNodeFilter();
        if (nodeFilter != null) {
            iter = into.getCachedNodes(nodeFilter).iterator();
        } else {
            iter = into.getNodes().iterator();
        }

        List<NodePosSmallPayload> toMove = new ObjectArrayList<>();
        while (iter.hasNext()) {
            toMove.add(GraphLibSyncingKNet.encodeNodePosSmall(iter.next().getPos(), data, palette, universe));
        }

        SplitPayload payload =
            new SplitPayload(new PayloadHeader(universe.getId(), palette, data), from.getId(), into.getId(),
                graphEntityIds, toMove);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        for (var iter1 = into.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(iter1.next().toChunkPos(), false));
        }
        for (var iter1 = from.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(
                world.getWorld().getChunkManager().delegate.getPlayersWatchingChunk(iter1.next().toChunkPos(), false));
        }

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                KNetChannels.SPLIT.sendPlay(player, payload);
            }
        }
    }

    public static void sendNodeRemove(BlockGraphImpl graph, NodeHolder<BlockNode> holder) {
        if (!(graph.getGraphView() instanceof GraphWorld world))
            throw new IllegalArgumentException("sendNodeRemove should only be called on the logical server");

        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(holder)) return;

        NodePosPayload nodePos = GraphLibSyncingKNet.encodeNodePos(holder.getPos(), universe);
        NodeRemovePayload payload = new NodeRemovePayload(universe.getId(), graph.getId(), nodePos);

        sendToFilteredWatching(KNetChannels.NODE_REMOVE, payload, world.getWorld(), holder.getBlockPos(), sp);
    }
}
