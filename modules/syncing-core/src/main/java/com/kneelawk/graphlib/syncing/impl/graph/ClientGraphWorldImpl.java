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

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphView;

public interface ClientGraphWorldImpl extends GraphView {
    void unload(ChunkPos pos);

    void setChunkMapCenter(int x, int z);

    void updateLoadDistance(int loadDistance);

    void readChunkPillar(int chunkX, int chunkZ, NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readNodeAdd(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readMerge(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readLink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readUnlink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readSplitInto(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;

    void readNodeRemove(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException;
}
