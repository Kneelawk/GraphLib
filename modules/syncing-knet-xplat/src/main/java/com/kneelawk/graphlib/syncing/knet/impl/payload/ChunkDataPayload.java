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

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.syncing.knet.impl.SyncingKNetImpl;
import com.kneelawk.knet.api.util.NetByteBuf;

public record ChunkDataPayload(PayloadHeader header, ChunkPos chunkPos, List<PayloadGraph> graphs)
    implements CustomPayload {
    public static final Id<ChunkDataPayload> ID = new Id<>(SyncingKNetImpl.id("chunk_data"));
    public static final PacketCodec<NetByteBuf, ChunkDataPayload> CODEC =
        PacketCodec.of(ChunkDataPayload::encode, ChunkDataPayload::decode);

    public static ChunkDataPayload decode(NetByteBuf buf) {
        PayloadHeader header = PayloadHeader.decode(buf);
        ChunkPos chunkPos = new ChunkPos(buf.readVarInt(), buf.readVarInt());

        int graphCount = buf.readVarUnsignedInt();
        List<PayloadGraph> graphs = new ObjectArrayList<>(graphCount);
        for (int i = 0; i < graphCount; i++) {
            graphs.add(PayloadGraph.decode(buf));
        }

        return new ChunkDataPayload(header, chunkPos, graphs);
    }

    public void encode(NetByteBuf buf) {
        header.encode(buf);

        buf.writeVarInt(chunkPos.x);
        buf.writeVarInt(chunkPos.z);

        buf.writeVarUnsignedInt(graphs.size());
        for (PayloadGraph graph : graphs) {
            graph.encode(buf);
        }
    }

    @Override
    public Id<?> getId() {
        return ID;
    }
}
