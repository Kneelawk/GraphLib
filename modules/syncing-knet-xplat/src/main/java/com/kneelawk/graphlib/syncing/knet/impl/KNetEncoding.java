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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
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
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.ChunkDataPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadExternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadGraph;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadHeader;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadInternalLink;
import com.kneelawk.graphlib.syncing.knet.impl.payload.PayloadNode;
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

    @SuppressWarnings("unchecked")
    public static void sendChunkData(ServerGraphWorldImpl world, ServerPlayerEntity player, ChunkPos chunkPos) {
        KNetSyncedUniverse universe = GraphLibSyncingKNet.getUniverse(world);

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
}
