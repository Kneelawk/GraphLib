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

package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Used for decoding {@link GraphEntity}s from packets.
 *
 * @param <G> the type of graph entity this packet decoder decodes.
 */
@FunctionalInterface
public interface GraphEntityPacketDecoder<G extends GraphEntity<G>> {
    /**
     * Decodes a {@link GraphEntity} from a {@link NetByteBuf} and {@link IMsgReadCtx}.
     *
     * @param buf    the buffer to read from.
     * @param msgCtx the message context.
     * @param ctx    the entity context for the new graph entity.
     * @return a new graph entity, or <code>null</code> if none could be decoded.
     */
    @Nullable G decode(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx msgCtx, @NotNull GraphEntityContext ctx);
}
