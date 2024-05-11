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

import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * A payload representing everything for a {@link LinkPos} that would not go in a header.
 *
 * @param first   the payload for the first node.
 * @param second  the payload for the second node.
 * @param typeId  the link key's type id.
 * @param linkBuf the buffer holding the link key's encoded data.
 */
public record LinkPosPayload(@NotNull NodePosPayload first, @NotNull NodePosPayload second,
                             @NotNull ResourceLocation typeId, @NotNull NetByteBuf linkBuf) {
    /**
     * This payload's codec.
     */
    public static final StreamCodec<NetByteBuf, LinkPosPayload> CODEC =
        StreamCodec.ofMember(LinkPosPayload::encode, LinkPosPayload::decode);

    /**
     * Decodes a payload from the buffer.
     *
     * @param buf the buffer to decode from.
     * @return the decoded payload.
     */
    public static LinkPosPayload decode(NetByteBuf buf) {
        NodePosPayload first = NodePosPayload.CODEC.decode(buf);
        NodePosPayload second = NodePosPayload.CODEC.decode(buf);
        ResourceLocation typeId = buf.readResourceLocation();

        int linkBufLen = buf.readInt();
        NetByteBuf linkBuf = NetBufs.netBuf(linkBufLen);
        buf.readBytes(linkBuf, linkBufLen);

        return new LinkPosPayload(first, second, typeId, linkBuf);
    }

    /**
     * Encodes this payload to the buffer.
     *
     * @param buf the buffer to encode to.
     */
    public void encode(NetByteBuf buf) {
        NodePosPayload.CODEC.encode(buf, first);
        NodePosPayload.CODEC.encode(buf, second);
        buf.writeResourceLocation(typeId);
        buf.writeInt(linkBuf.readableBytes());
        buf.writeBytes(linkBuf, linkBuf.readerIndex(), linkBuf.readableBytes());
    }
}
