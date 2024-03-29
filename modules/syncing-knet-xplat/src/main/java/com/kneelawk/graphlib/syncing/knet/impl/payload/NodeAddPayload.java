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

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.syncing.knet.impl.KNetChannels;
import com.kneelawk.knet.api.channel.NetPayload;
import com.kneelawk.knet.api.util.NetByteBuf;

public record NodeAddPayload(PayloadHeader header, long graphId, int[] graphEntityIds, PayloadNode node)
    implements NetPayload {
    public static NodeAddPayload decode(NetByteBuf buf) {
        PayloadHeader header = PayloadHeader.decode(buf);
        long graphId = buf.readVarUnsignedLong();
        int[] graphEntityIds = PayloadUtils.readVarUnsignedIntArray(buf);
        PayloadNode node = PayloadNode.decode(buf);

        return new NodeAddPayload(header, graphId, graphEntityIds, node);
    }

    @Override
    public void write(NetByteBuf buf) {
        header.encode(buf);
        buf.writeVarUnsignedLong(graphId);
        PayloadUtils.writeVarUnsignedIntArray(graphEntityIds, buf);
        node.encode(buf);
    }

    @Override
    public Identifier id() {
        return KNetChannels.NODE_ADD.getId();
    }
}
