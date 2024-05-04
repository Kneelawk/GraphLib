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

package com.kneelawk.graphlib.syncing.knet.impl.payload;

import com.kneelawk.graphlib.syncing.knet.impl.SyncingKNetImpl;
import com.kneelawk.knet.api.util.NetByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NodeAddPayload(PayloadHeader header, long graphId, PayloadNode node, int[] graphEntityIds)
    implements CustomPacketPayload {
    public static final Type<NodeAddPayload> ID = new Type<>(SyncingKNetImpl.id("node_add"));
    public static final StreamCodec<NetByteBuf, NodeAddPayload> CODEC =
        StreamCodec.ofMember(NodeAddPayload::encode, NodeAddPayload::decode);

    public static NodeAddPayload decode(NetByteBuf buf) {
        PayloadHeader header = PayloadHeader.decode(buf);
        long graphId = buf.readVarUnsignedLong();
        int[] graphEntityIds = PayloadUtils.readVarUnsignedIntArray(buf);
        PayloadNode node = PayloadNode.decode(buf);

        return new NodeAddPayload(header, graphId, node, graphEntityIds);
    }

    public void encode(NetByteBuf buf) {
        header.encode(buf);
        buf.writeVarUnsignedLong(graphId);
        PayloadUtils.writeVarUnsignedIntArray(graphEntityIds, buf);
        node.encode(buf);
    }

    @Override
    public Type<?> type() {
        return ID;
    }
}
