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

import java.util.BitSet;
import java.util.Objects;
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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.ClientGraphWorldImpl;

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
    public void receiveChunkPillar(int chunkX, int chunkZ, NetByteBuf buf, IMsgReadCtx ctx) {
        SimpleBlockGraphPillar pillar = manager.getOrCreatePillar(chunkX, chunkZ);
        if (pillar == null) {
            GLLog.warn("Received pillar outside current client range at ({}, {})", chunkX, chunkZ);
            ctx.drop("Pillar outside range");
            return;
        }

        BitSet vertical = buf.readBitSet();

        int chunkCount = world.getTopSectionCoord() - world.getBottomSectionCoord();
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            if (!vertical.get(chunkIndex)) continue;

            int chunkY = chunkIndex + world.getBottomSectionCoord();
            SimpleBlockGraphChunk chunk = pillar.getOrCreate(chunkY);

            int chunkBufLen = buf.readVarUnsignedInt();
            NetByteBuf chunkBuf = buf.readBytes(chunkBufLen);

            int graphCount = chunkBuf.readVarUnsignedInt();
            for (int graphIndex = 0; graphIndex < graphCount; graphIndex++) {
                long graphId = chunkBuf.readVarUnsignedLong();
                SimpleBlockGraph graph = getOrCreateGraph(graphId);

                // load graph entities if they're not already loaded
                int graphEntityBufLen = chunkBuf.readVarUnsignedInt();
                if (graphEntityBufLen > 0) {
                    NetByteBuf graphEntityBuf = chunkBuf.readBytes(graphEntityBufLen);
                    graph.loadGraphEntitiesFromPacket(graphEntityBuf, ctx);
                }

                // TODO: graph reading
            }
        }
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
}
