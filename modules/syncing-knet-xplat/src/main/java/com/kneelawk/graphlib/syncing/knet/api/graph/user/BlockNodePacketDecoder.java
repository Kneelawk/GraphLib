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

package com.kneelawk.graphlib.syncing.knet.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * Used for decoding a {@link BlockNode} from a {@link NetByteBuf}.
 */
@FunctionalInterface
public interface BlockNodePacketDecoder {
    /**
     * Decodes a {@link BlockNode} from a {@link NetByteBuf}.
     * <p>
     * The data read should be the same data written by
     * {@link BlockNodePacketEncoder#encode(BlockNode, NetByteBuf)}.
     *
     * @param buf the buffer to decode from.
     * @return the decoded block node.
     * @throws PayloadHandlingException if a block node could not be decoded.
     */
    @NotNull BlockNode decode(@NotNull NetByteBuf buf) throws PayloadHandlingException;
}
