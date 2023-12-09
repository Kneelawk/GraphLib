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
import org.jetbrains.annotations.Nullable;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Holds a block node encoder and decoder.
 *
 * @param encoder the encoder.
 * @param decoder the decoder.
 */
public record BlockNodeSyncing(@NotNull BlockNodePacketEncoder<?> encoder, @NotNull BlockNodePacketDecoder decoder) {
    /**
     * Encodes a block node.
     * <p>
     * <b>Note: this does not write the block node's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the block node being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the block node to encode.
     * @param buf  the buffer to encode to.
     * @param ctx  the message context.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull BlockNode node, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {
        ((BlockNodePacketEncoder<BlockNode>) encoder).encode(node, buf, ctx);
    }

    /**
     * Decodes a block node.
     *
     * @param buf the buffer to decode from.
     * @param ctx the message context.
     * @return a newly decoded block node.
     * @throws InvalidInputDataException if the buffer contained invalid data.
     */
    public @NotNull BlockNode decode(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx ctx)
        throws InvalidInputDataException {
        return decoder.decode(buf, ctx);
    }
}
