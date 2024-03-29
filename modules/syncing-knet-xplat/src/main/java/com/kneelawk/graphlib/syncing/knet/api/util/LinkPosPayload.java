/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.knet.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * A payload representing everything for a {@link LinkPos} that would not go in a header.
 *
 * @param first   the payload for the first node.
 * @param second  the payload for the second node.
 * @param typeId  the link key's type id.
 * @param linkBuf the buffer holding the link key's encoded data.
 */
public record LinkPosPayload(@NotNull NodePosPayload first, @NotNull NodePosPayload second,
                             @NotNull Identifier typeId, @NotNull NetByteBuf linkBuf) {
    /**
     * This payload's codec.
     */
    public static final PayloadCodec<LinkPosPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
        NodePosPayload.CODEC.encoder().accept(buf, payload.first);
        NodePosPayload.CODEC.encoder().accept(buf, payload.second);
        buf.writeIdentifier(payload.typeId);
        buf.writeInt(payload.linkBuf.readableBytes());
        buf.writeBytes(payload.linkBuf, payload.linkBuf.readerIndex(), payload.linkBuf.readableBytes());
    }, buf -> {
        NodePosPayload first = NodePosPayload.CODEC.decoder().apply(buf);
        NodePosPayload second = NodePosPayload.CODEC.decoder().apply(buf);
        Identifier typeId = buf.readIdentifier();

        int linkBufLen = buf.readInt();
        NetByteBuf linkBuf = NetByteBuf.buffer(linkBufLen);
        buf.readBytes(linkBuf, linkBufLen);

        return new LinkPosPayload(first, second, typeId, linkBuf);
    });
}
