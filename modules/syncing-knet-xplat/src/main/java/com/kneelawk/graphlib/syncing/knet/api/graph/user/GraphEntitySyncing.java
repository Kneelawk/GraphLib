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

import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * Holds a graph entity encoder and decoder.
 *
 * @param <G> the type of graph entity this syncs.
 */
public final class GraphEntitySyncing<G extends GraphEntity<G>> {
    private final @NotNull GraphEntityPacketEncoder<G> encoder;
    private final @NotNull GraphEntityPacketDecoder decoder;

    private GraphEntitySyncing(@NotNull GraphEntityPacketEncoder<G> encoder,
                               @NotNull GraphEntityPacketDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Encodes a graph entity.
     * <p>
     * <b>Note: this does not write the graph entity's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the graph entity being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the graph entity to encode.
     * @param buf  the buffer to encode to.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull GraphEntity<?> node, @NotNull NetByteBuf buf) {
        encoder.encode((G) node, buf);
    }

    /**
     * Decodes a graph entity.
     *
     * @param buf the buffer to decode from.
     * @return a newly decoded graph entity.
     * @throws PayloadHandlingException if the buffer contained invalid data.
     */
    public @NotNull GraphEntity<?> decode(@NotNull NetByteBuf buf) throws PayloadHandlingException {
        return decoder.decode(buf);
    }

    /**
     * Makes a new {@link GraphEntity} syncing descriptor.
     *
     * @param encoder the encoder for the graph entity.
     * @param decoder the decoder for the graph entity.
     * @param <G>     the type of graph entity this descriptor syncs.
     * @return a new graph entity syncing descriptor.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> of(
        @NotNull GraphEntityPacketEncoder<G> encoder, @NotNull GraphEntityPacketDecoder decoder) {
        return new GraphEntitySyncing<>(encoder, decoder);
    }

    /**
     * Makes a new {@link GraphEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param supplier supplies instances of the graph entity.
     * @param <G>      the type of graph entity this descriptor syncs.
     * @return a new graph entity syncing descriptor.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> ofNoOp(@NotNull Supplier<G> supplier) {
        return new GraphEntitySyncing<G>(GraphEntityPacketEncoder.noOp(), buf -> supplier.get());
    }
}