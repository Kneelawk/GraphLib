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

import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * Holds a block node encoder and decoder.
 */
public final class BlockNodeSyncing {
    private final @NotNull BlockNodePacketEncoder<?> encoder;
    private final @NotNull BlockNodePacketDecoder decoder;

    private BlockNodeSyncing(@NotNull BlockNodePacketEncoder<?> encoder, @NotNull BlockNodePacketDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Encodes a block node.
     * <p>
     * <b>Note: this does not write the block node's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the block node being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the block node to encode.
     * @param buf  the buffer to encode to.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull BlockNode node, @NotNull PacketByteBuf buf) {
        ((BlockNodePacketEncoder<BlockNode>) encoder).encode(node, buf);
    }

    /**
     * Decodes a block node.
     *
     * @param buf the buffer to decode from.
     * @return a newly decoded block node.
     * @throws PayloadHandlingException if the buffer contained invalid data.
     */
    public @NotNull BlockNode decode(@NotNull PacketByteBuf buf) throws PayloadHandlingException {
        return decoder.decode(buf);
    }

    /**
     * Makes a {@link BlockNode} syncing descriptor.
     *
     * @param encoder the encoder.
     * @param decoder the decoder.
     * @param <N>     the type of block node this descriptor syncs.
     * @return a new block node syncing descriptor.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <N extends BlockNode> @NotNull BlockNodeSyncing of(@NotNull BlockNodePacketEncoder<N> encoder,
                                                                     @NotNull BlockNodePacketDecoder decoder) {
        return new BlockNodeSyncing(encoder, decoder);
    }

    /**
     * Makes a {@link BlockNode} syncing descriptor that does no encoding or decoding.
     *
     * @param supplier supplies the instance(s) of the block node.
     * @return a new block node syncing descriptor.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull BlockNodeSyncing ofNoOp(@NotNull Supplier<? extends BlockNode> supplier) {
        return new BlockNodeSyncing(BlockNodePacketEncoder.noOp(), buf -> supplier.get());
    }
}
