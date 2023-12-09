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

package com.kneelawk.graphlib.syncing.api.graph.user;

import org.jetbrains.annotations.NotNull;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Used for encoding a {@link BlockNode} to a {@link NetByteBuf}.
 *
 * @param <N> the type of block node this encoder encodes.
 */
@FunctionalInterface
public interface BlockNodePacketEncoder<N extends BlockNode> {
    /**
     * Encodes a {@link BlockNode} to a {@link NetByteBuf}.
     * <p>
     * This data will be decoded by {@link BlockNodePacketDecoder#decode(NetByteBuf, IMsgReadCtx)}.
     *
     * @param node the node to encode.
     * @param buf  the buffer to write to.
     * @param ctx  the message write context.
     */
    void encode(@NotNull N node, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx);
}
