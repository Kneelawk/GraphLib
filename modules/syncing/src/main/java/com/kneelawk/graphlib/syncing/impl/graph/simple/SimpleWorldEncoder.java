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

package com.kneelawk.graphlib.syncing.impl.graph.simple;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.simple.SimpleBlockGraph;
import com.kneelawk.graphlib.impl.graph.simple.SimpleBlockGraphChunk;
import com.kneelawk.graphlib.syncing.impl.GLNet;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;
import com.kneelawk.graphlib.syncing.impl.graph.WorldEncoder;

public class SimpleWorldEncoder implements WorldEncoder {
    private final SimpleSyncedUniverse universe;
    private final ServerGraphWorldImpl world;

    public SimpleWorldEncoder(SimpleSyncedUniverse universe, ServerGraphWorldImpl world) {
        this.universe = universe;
        this.world = world;
    }

    @Override
    public SyncedUniverseImpl getUniverse() {
        return universe;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeChunkPillar(ChunkPos chunkPos, NetByteBuf buf, IMsgWriteCtx ctx) {
        // collect graphs to encode
        Long2ObjectMap<SimpleBlockGraph> toEncode = new Long2ObjectLinkedOpenHashMap<>();
        for (int chunkY = world.getWorld().getBottomSectionCoord(); chunkY < world.getWorld().getTopSectionCoord(); chunkY++) {
            SimpleBlockGraphChunk chunk = chunks.getIfExists(ChunkSectionPos.from(chunkPos, chunkY));
            if (chunk != null) {
                for (long graphId : chunk.getGraphs()) {
                    SimpleBlockGraph graph = getGraph(graphId);
                    if (graph != null) {
                        toEncode.put(graphId, graph);
                    }
                }
            }
        }

        // write graphs
        buf.writeVarUnsignedInt(toEncode.size());
        for (SimpleBlockGraph graph : toEncode.values()) {
            buf.writeMarker("gs");

            // write the graph id
            buf.writeVarUnsignedLong(graph.getId());

            // write graph entities if any exist
            graph.writeGraphEntitiesToPacket(buf, ctx);

            buf.writeMarker("n");

            Object2IntMap<NodePos> indexMap = new Object2IntLinkedOpenHashMap<>();
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

            // save an index before everything where we'll write nodeCount once we're done
            int nodeCountIndex = buf.writerIndex();
            buf.writeInt(0);

            int nodeCount = 0;
            while (iter.hasNext()) {
                NodeHolder<BlockNode> holder = iter.next();
                BlockPos blockPos = holder.getBlockPos();

                if (blockPos.getX() < chunkPos.getStartX() || chunkPos.getEndX() < blockPos.getX() ||
                    blockPos.getZ() < chunkPos.getStartZ() || chunkPos.getEndZ() < blockPos.getZ()) {
                    continue;
                }

                holder.getPos().toPacket(buf, ctx);

                writeNodeEntity(holder, buf, ctx, graph);

                // put the node into the index map for links to look up
                indexMap.put(holder.getPos(), nodeCount);

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

                nodeCount++;
            }
            buf.setInt(nodeCountIndex, nodeCount);

            buf.writeMarker("il");

            // save internal links count index too
            int iLinkCountIndex = buf.writerIndex();
            buf.writeInt(0);
            int iLinkCount = 0;
            for (LinkPos link : internalLinks) {
                int nodeAIndex = indexMap.getInt(link.first());
                int nodeBIndex = indexMap.getInt(link.second());

                if (nodeAIndex < 0 || nodeBIndex < 0) {
                    GLLog.warn(
                        "Tried to send an internal link to a node that does not exist within the same chunk. Link: {}",
                        link);
                    continue;
                }

                buf.writeVarUnsignedInt(nodeAIndex);
                buf.writeVarUnsignedInt(nodeBIndex);

                LinkKey linkKey = link.key();
                GLNet.writeType(buf, ctx.getConnection(), linkKey.getType().getId());
                linkKey.toPacket(buf, ctx);

                // quarantine the link entity for the same reason as node entities
                writeLinkEntity(buf, ctx, link, graph);

                iLinkCount++;
            }
            buf.setInt(iLinkCountIndex, iLinkCount);

            buf.writeMarker("el");

            // write external links
            buf.writeVarUnsignedInt(externalLinks.size());
            for (LinkPos link : externalLinks) {
                link.toPacket(buf, ctx);

                // quarantine link entities
                writeLinkEntity(buf, ctx, link, graph);
            }

            buf.writeMarker("ge");
        }
    }

    private static void writeNodeEntity(NodeHolder<BlockNode> node, NetByteBuf buf, IMsgWriteCtx ctx,
                                        BlockGraph graph) {
        NodeEntity entity = graph.getNodeEntity(node.getPos());
        if (entity != null) {
            buf.writeBoolean(true);
            GLNet.writeType(buf, ctx.getConnection(), entity.getType().getId());
            entity.toPacket(buf, ctx);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static void writeLinkEntity(NetByteBuf buf, IMsgWriteCtx ctx, LinkPos link, BlockGraph graph) {
        LinkEntity entity = graph.getLinkEntity(link);
        if (entity != null) {
            buf.writeBoolean(true);
            GLNet.writeType(buf, ctx.getConnection(), entity.getType().getId());
            entity.toPacket(buf, ctx);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void writeNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node, NetByteBuf buf, IMsgWriteCtx ctx) {
        node.getPos().toPacket(buf, ctx);

        buf.writeVarUnsignedLong(graph.getId());

        ((SimpleBlockGraph) graph).writeGraphEntitiesToPacket(buf, ctx);

        writeNodeEntity(node, buf, ctx, graph);
    }

    @Override
    public void writeMerge(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedLong(from.getId());

        buf.writeVarUnsignedLong(into.getId());

        ((SimpleBlockGraph) into).writeGraphEntitiesToPacket(buf, ctx);
    }

    @Override
    public void writeLink(BlockGraph graph, LinkHolder<LinkKey> link, NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedLong(graph.getId());

        LinkPos linkPos = link.getPos();
        linkPos.toPacket(buf, ctx);

        writeLinkEntity(buf, ctx, linkPos, graph);
    }

    @Override
    public void writeUnlink(BlockGraph graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key,
                            NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedLong(graph.getId());

        LinkPos linkPos = new LinkPos(a.getPos(), b.getPos(), key);
        linkPos.toPacket(buf, ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeSplitInto(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedLong(from.getId());

        buf.writeVarUnsignedLong(into.getId());

        ((SimpleBlockGraph) into).writeGraphEntitiesToPacket(buf, ctx);

        // iterate over only the nodes we want to synchronize
        Iterator<NodeHolder<BlockNode>> iter;
        int nodeCount;
        CacheCategory<BlockNode> nodeFilter = (CacheCategory<BlockNode>) universe.getSyncProfile().getNodeFilter();
        if (nodeFilter != null) {
            Collection<NodeHolder<BlockNode>> cachedNodes = into.getCachedNodes(nodeFilter);
            iter = cachedNodes.iterator();
            nodeCount = cachedNodes.size();
        } else {
            iter = into.getNodes().iterator();
            nodeCount = into.size();
        }

        buf.writeVarUnsignedInt(nodeCount);
        while (iter.hasNext()) {
            NodeHolder<BlockNode> holder = iter.next();
            holder.getPos().toPacket(buf, ctx);
        }
    }

    @Override
    public void writeNodeRemove(BlockGraph graph, NodeHolder<BlockNode> holder, NetByteBuf buf, IMsgWriteCtx ctx) {
        buf.writeVarUnsignedLong(graph.getId());

        holder.getPos().toPacket(buf, ctx);
    }

    @Override
    public void sendNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node) {
        GLNet.sendNodeAdd(graph, node);
    }

    @Override
    public void sendMerge(BlockGraph from, BlockGraph into) {
        GLNet.sendMerge(from, into);
    }

    @Override
    public void sendLink(BlockGraph graph, LinkHolder<LinkKey> link) {
        GLNet.sendLink(graph, link);
    }

    @Override
    public void sendUnlink(BlockGraph graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {
        GLNet.sendUnlink(graph, a, b, key);
    }

    @Override
    public void sendSplitInto(BlockGraph from, BlockGraph into) {
        GLNet.sendSplitInto(from, into);
    }

    @Override
    public void sendNodeRemove(BlockGraph graph, NodeHolder<BlockNode> holder) {
        GLNet.sendNodeRemove(graph, holder);
    }
}
