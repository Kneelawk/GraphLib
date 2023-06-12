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

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

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
import com.kneelawk.graphlib.impl.graph.ClientGraphWorldImpl;

public class SimpleClientGraphWorld implements GraphView, ClientGraphWorldImpl, SimpleGraphCollection {
    private final SimpleGraphUniverse universe;
    final World world;

    private final SimpleClientGraphChunkManager manager;

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
    public @NotNull GraphUniverse getUniverse() {
        return universe;
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos) {
        return null;
    }

    @Override
    public @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos) {
        return null;
    }

    @Override
    public boolean nodeExistsAt(@NotNull NodePos pos) {
        return false;
    }

    @Override
    public @Nullable BlockGraph getGraphForNode(@NotNull NodePos pos) {
        return null;
    }

    @Override
    public @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos) {
        return null;
    }

    @Override
    public boolean linkExistsAt(@NotNull LinkPos pos) {
        return false;
    }

    @Override
    public @Nullable LinkHolder<LinkKey> getLinkAt(@NotNull LinkPos pos) {
        return null;
    }

    @Override
    public @Nullable LinkEntity getLinkEntity(@NotNull LinkPos pos) {
        return null;
    }

    @Override
    public @NotNull LongStream getAllGraphIdsAt(@NotNull BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsAt(@NotNull BlockPos pos) {
        return null;
    }

    @Override
    public @Nullable BlockGraph getGraph(long id) {
        return null;
    }

    @Override
    public @NotNull LongStream getAllGraphIdsInChunkSection(@NotNull ChunkSectionPos pos) {
        return null;
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsInChunkSection(@NotNull ChunkSectionPos pos) {
        return null;
    }

    @Override
    public @NotNull LongStream getAllGraphIdsInChunk(@NotNull ChunkPos pos) {
        return null;
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphsInChunk(@NotNull ChunkPos pos) {
        return null;
    }

    @Override
    public @NotNull LongStream getAllGraphIds() {
        return null;
    }

    @Override
    public @NotNull Stream<BlockGraph> getLoadedGraphs() {
        return null;
    }

    private void onUnload(SimpleBlockGraphPillar pillar) {

    }
}
