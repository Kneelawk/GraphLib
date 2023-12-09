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

package com.kneelawk.graphlib.syncing.impl.graph;

import net.minecraft.util.math.ChunkPos;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;

public interface WorldEncoder extends WorldListener {
    SyncedUniverseImpl getUniverse();

    void writeChunkPillar(ChunkPos pos, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeMerge(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeLink(BlockGraph graph, LinkHolder<LinkKey> link, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeUnlink(BlockGraph graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key, NetByteBuf buf,
                     IMsgWriteCtx ctx);

    void writeSplitInto(BlockGraph from, BlockGraph into, NetByteBuf buf, IMsgWriteCtx ctx);

    void writeNodeRemove(BlockGraph graph, NodeHolder<BlockNode> holder, NetByteBuf buf, IMsgWriteCtx ctx);
}
