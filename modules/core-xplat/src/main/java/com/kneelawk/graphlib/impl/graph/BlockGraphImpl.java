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

package com.kneelawk.graphlib.impl.graph;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.NodePos;

public interface BlockGraphImpl extends BlockGraph {
    @NotNull NbtCompound toTag();

    void initializeGraphEntities(List<GraphEntity<?>> newGraphEntities);

    LongSet getChunksImpl();

    @NotNull NodeHolder<BlockNode> createNode(@NotNull BlockPos blockPos, @NotNull BlockNode node,
                                           @Nullable NodeEntity entity, boolean newlyAdded);

    void destroyNode(@NotNull NodeHolder<BlockNode> holder, boolean doSplit);

    @NotNull LinkHolder<LinkKey> link(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key,
                                      @Nullable LinkEntity entity, boolean newlyAdded);

    boolean unlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b, LinkKey key);

    void merge(@NotNull BlockGraphImpl other);

    @NotNull List<? extends BlockGraphImpl> split();

    void splitInto(BlockGraphImpl into, Collection<NodePos> nodes);

    void unloadInChunk(int chunkX, int chunkZ);

    void onUnload();

    void onDestroy();

    void onTick();
}
