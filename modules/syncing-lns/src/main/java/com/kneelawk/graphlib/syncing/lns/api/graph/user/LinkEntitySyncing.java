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

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.LinkEntity;

/**
 * Holds a link entity encoder and decoder.
 */
public final class LinkEntitySyncing {
    private final @NotNull LinkEntityPacketEncoder<?> encoder;
    private final @NotNull LinkEntityPacketDecoder decoder;

    private LinkEntitySyncing(@NotNull LinkEntityPacketEncoder<?> encoder, @NotNull LinkEntityPacketDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Encodes a link entity.
     * <p>
     * <b>Note: this does not write the link entity's type id. That must be written separately.</b>
     * <p>
     * <b>Note: the link entity being encoded must be of the type that the encoder expects.</b>
     *
     * @param node the link entity to encode.
     * @param buf  the buffer to encode to.
     * @param ctx  the message context.
     */
    @SuppressWarnings("unchecked")
    public void encode(@NotNull LinkEntity node, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {
        ((LinkEntityPacketEncoder<LinkEntity>) encoder).encode(node, buf, ctx);
    }

    /**
     * Decodes a link entity.
     *
     * @param buf the buffer to decode from.
     * @param ctx the message context.
     * @return a newly decoded link entity.
     * @throws InvalidInputDataException if the buffer contained invalid data.
     */
    public @NotNull LinkEntity decode(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx ctx)
        throws InvalidInputDataException {
        return decoder.decode(buf, ctx);
    }

    /**
     * Makes a {@link LinkEntity} syncing descriptor.
     *
     * @param encoder the encoder.
     * @param decoder the decoder.
     * @param <L>     the type of link entity this descriptor syncs.
     * @return a new link entity syncing descriptor.
     */
    public static <L extends LinkEntity> @NotNull LinkEntitySyncing of(@NotNull LinkEntityPacketEncoder<L> encoder,
                                                                       @NotNull LinkEntityPacketDecoder decoder) {
        return new LinkEntitySyncing(encoder, decoder);
    }

    /**
     * Makes a {@link LinkEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param supplier supplies new instances of the link entity.
     * @return a new link entity syncing descriptor.
     */
    public static @NotNull LinkEntitySyncing ofNoOp(@NotNull Supplier<? extends LinkEntity> supplier) {
        return new LinkEntitySyncing(LinkEntityPacketEncoder.noOp(), (buf, msgCtx) -> supplier.get());
    }
}