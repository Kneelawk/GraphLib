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

import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * Holds a link key encoder and decoder.
 */
public final class LinkKeySyncing {
    private final @NotNull LinkKeyPacketEncoder<?> encoder;
    private final @NotNull LinkKeyPacketDecoder decoder;

    private LinkKeySyncing(@NotNull LinkKeyPacketEncoder<?> encoder, @NotNull LinkKeyPacketDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Encodes a link key.
     * <p>
     * <b>Note: this does not write the link key's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the link key being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the link key to encode.
     * @param buf  the buffer to encode to.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull LinkKey node, @NotNull PacketByteBuf buf) {
        ((LinkKeyPacketEncoder<LinkKey>) encoder).encode(node, buf);
    }

    /**
     * Decodes a link key.
     *
     * @param buf the buffer to decode from.
     * @return a newly decoded link key.
     * @throws PayloadHandlingException if the buffer contained invalid data.
     */
    public @NotNull LinkKey decode(@NotNull PacketByteBuf buf) throws PayloadHandlingException {
        return decoder.decode(buf);
    }

    /**
     * Makes a {@link LinkKey} syncing descriptor.
     *
     * @param encoder the encoder.
     * @param decoder the decoder.
     * @param <L>     the type of link key this descriptor syncs.
     * @return a link key syncing descriptor.
     */
    public static <L extends LinkKey> @NotNull LinkKeySyncing of(@NotNull LinkKeyPacketEncoder<L> encoder,
                                                                 @NotNull LinkKeyPacketDecoder decoder) {
        return new LinkKeySyncing(encoder, decoder);
    }

    /**
     * Makes a {@link LinkKey} syncing descriptor that does not do any encoding or decoding.
     *
     * @param supplier supplies the instance(s) of the link key.
     * @return a link key syncing descriptor.
     */
    public static @NotNull LinkKeySyncing ofNoOp(@NotNull Supplier<? extends LinkKey> supplier) {
        return new LinkKeySyncing(LinkKeyPacketEncoder.noOp(), buf -> supplier.get());
    }
}
