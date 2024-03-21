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

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * Holds a node entity encoder and decoder.
 */
public final class NodeEntitySyncing {
    private final @NotNull NodeEntityPacketEncoder<?> encoder;
    private final @NotNull NodeEntityPacketDecoder decoder;

    private NodeEntitySyncing(@NotNull NodeEntityPacketEncoder<?> encoder, @NotNull NodeEntityPacketDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Encodes a node entity.
     * <p>
     * <b>Note: this does not write the node entity's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the node entity being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the node entity to encode.
     * @param buf  the buffer to encode to.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull NodeEntity node, @NotNull PacketByteBuf buf) {
        ((NodeEntityPacketEncoder<NodeEntity>) encoder).encode(node, buf);
    }

    /**
     * Decodes a node entity.
     *
     * @param buf the buffer to decode from.
     * @return a newly decoded node entity.
     * @throws PayloadHandlingException if the buffer contained invalid data.
     */
    public @NotNull NodeEntity decode(@NotNull PacketByteBuf buf) throws PayloadHandlingException {
        return decoder.decode(buf);
    }

    /**
     * Makes a {@link NodeEntity} syncing descriptor.
     *
     * @param encoder the encoder.
     * @param decoder the decoder.
     * @param <N>     the type of node entity this descriptor syncs.
     * @return a new node entity syncing descriptor.
     */
    public static <N extends NodeEntity> @NotNull NodeEntitySyncing of(@NotNull NodeEntityPacketEncoder<N> encoder,
                                                                       @NotNull NodeEntityPacketDecoder decoder) {
        return new NodeEntitySyncing(encoder, decoder);
    }

    /**
     * Makes a {@link NodeEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param supplier supplies new instances of node entities.
     * @return a new node entity syncing descriptor.
     */
    public static @NotNull NodeEntitySyncing ofNoOp(@NotNull Supplier<? extends NodeEntity> supplier) {
        return new NodeEntitySyncing(NodeEntityPacketEncoder.noOp(), buf -> supplier.get());
    }
}
