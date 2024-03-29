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
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * A payload representing everything for a {@link NodePos} that would not go in the header.
 *
 * @param pos     the block position.
 * @param typeId  the node's type id.
 * @param nodeBuf the buffer holding the node's encoded data.
 */
public record NodePosPayload(@NotNull BlockPos pos, @NotNull Identifier typeId, @NotNull NetByteBuf nodeBuf) {
    /**
     * This payload's codec.
     */
    public static final PayloadCodec<NodePosPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
        buf.writeBlockPos(payload.pos);
        buf.writeIdentifier(payload.typeId);
        buf.writeInt(payload.nodeBuf.readableBytes());
        buf.writeBytes(payload.nodeBuf, payload.nodeBuf.readerIndex(), payload.nodeBuf.readableBytes());
    }, buf -> {
        BlockPos pos = buf.readBlockPos();
        Identifier typeId = buf.readIdentifier();

        int nodeBufLen = buf.readInt();
        NetByteBuf nodeBuf = NetByteBuf.buffer(nodeBufLen);
        buf.readBytes(nodeBuf, nodeBufLen);

        return new NodePosPayload(pos, typeId, nodeBuf);
    });
}
