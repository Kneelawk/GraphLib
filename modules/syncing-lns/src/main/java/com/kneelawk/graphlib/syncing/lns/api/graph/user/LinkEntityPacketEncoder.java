/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.api.graph.user;

import org.jetbrains.annotations.NotNull;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.LinkEntity;

/**
 * Used for encoding a {@link LinkEntity} to a {@link NetByteBuf}.
 *
 * @param <L> the type of link entity this encoder encodes.
 */
@FunctionalInterface
public interface LinkEntityPacketEncoder<L extends LinkEntity> {
    /**
     * Returns a no-op encoder.
     *
     * @param <T> the type of link entity to encode.
     * @return a no-op encoder.
     */
    static <T extends LinkEntity> LinkEntityPacketEncoder<T> noOp() {
        return (link, buf, ctx) -> {};
    }

    /**
     * Encodes a {@link LinkEntity} to a {@link NetByteBuf}.
     * <p>
     * The data will be decoded by {@link LinkEntityPacketDecoder#decode(NetByteBuf, IMsgReadCtx)}.
     *
     * @param link the link entity to be encoded.
     * @param buf  the buffer to write to.
     * @param ctx  the message context.
     */
    void encode(@NotNull L link, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx);
}
