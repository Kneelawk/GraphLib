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

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.LongIterable;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;

public interface SimpleGraphCollection extends GraphView {
    /**
     * Marks a graph as dirty and in need of saving.
     *
     * @param graphId the id of the graph to mark.
     */
    void markDirty(long graphId);

    /**
     * Creates a new graph and stores it, assigning it an ID.
     *
     * @return the newly-created graph.
     */
    @NotNull SimpleBlockGraph createGraph(boolean initializeGraphEntities);

    /**
     * Deletes a graph and all nodes it contains.
     *
     * @param id the ID of the graph to delete.
     */
    void destroyGraph(long id);

    void putGraphWithNode(long id, @NotNull NodePos pos);

    void removeGraphWithNode(long id, @NotNull NodePos pos);

    void removeGraphInPos(long id, @NotNull BlockPos pos);

    void removeGraphInChunk(long id, long pos);

    void removeGraphInPoses(long id, @NotNull Iterable<NodePos> nodes, @NotNull Iterable<BlockPos> poses,
                            @NotNull LongIterable chunkPoses);

    void scheduleCallbackUpdate(@NotNull NodeHolder<BlockNode> node, boolean validate);

    void graphUpdated(SimpleBlockGraph graph);

    void sendNodeAdd(BlockGraphImpl graph, NodeHolder<BlockNode> node);

    void sendMerge(BlockGraphImpl from, BlockGraphImpl into);

    void sendLink(BlockGraphImpl graph, LinkHolder<LinkKey> link);

    void sendUnlink(BlockGraphImpl graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key);

    void sendSplitInto(BlockGraphImpl from, BlockGraphImpl into);

    void sendNodeRemove(BlockGraphImpl graph, NodeHolder<BlockNode> holder);
}
