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

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.syncing.knet.api.util.NoOpPayload;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * Holds a block node encoder and decoder.
 */
public final class BlockNodeSyncing {
    private final @NotNull BlockNodePacketEncoder<?, ?> encoder;
    private final @NotNull BlockNodePacketDecoder<?> decoder;
    private final @NotNull PayloadCodec<?> payloadCodec;

    private BlockNodeSyncing(@NotNull BlockNodePacketEncoder<?, ?> encoder, @NotNull BlockNodePacketDecoder<?> decoder,
                             @NotNull PayloadCodec<?> payloadCodec) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.payloadCodec = payloadCodec;
    }

    /**
     * Encodes a block node.
     * <p>
     * <b>Note: this does not write the block node's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the block node being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the block node to encode.
     */
    @SuppressWarnings("unchecked")
    public Object encode(@NotNull BlockNode node) {
        return ((BlockNodePacketEncoder<BlockNode, Object>) encoder).encode(node);
    }

    /**
     * Decodes a block node.
     *
     * @param payload the payload to decode from.
     * @return a newly decoded block node.
     * @throws PayloadHandlingException if the buffer contained invalid data.
     */
    @SuppressWarnings("unchecked")
    public @NotNull BlockNode decode(@NotNull Object payload) throws PayloadHandlingException {
        return ((BlockNodePacketDecoder<Object>) decoder).decode(payload);
    }

    /**
     * Makes a {@link BlockNode} syncing descriptor.
     *
     * @param encoder      the encoder.
     * @param decoder      the decoder.
     * @param payloadCodec the codec for the payload.
     * @param <N>          the type of block node this descriptor syncs.
     * @param <P>          the type of payload encoded.
     * @return a new block node syncing descriptor.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <N extends BlockNode, P> @NotNull BlockNodeSyncing of(@NotNull BlockNodePacketEncoder<N, P> encoder,
                                                                        @NotNull BlockNodePacketDecoder<P> decoder,
                                                                        @NotNull PayloadCodec<P> payloadCodec) {
        return new BlockNodeSyncing(encoder, decoder, payloadCodec);
    }

    /**
     * Makes a {@link BlockNode} syncing descriptor that does no encoding or decoding.
     *
     * @param supplier supplies the instance(s) of the block node.
     * @return a new block node syncing descriptor.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull BlockNodeSyncing ofNoOp(@NotNull Supplier<? extends BlockNode> supplier) {
        return new BlockNodeSyncing(BlockNodePacketEncoder.noOp(), payload -> supplier.get(), NoOpPayload.CODEC);
    }
}
