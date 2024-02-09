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

package com.kneelawk.graphlib.debugrender.impl.payload;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;

public record GraphUpdateBulkPayload(PayloadHeader header, List<PayloadGraph> graphs) implements CustomPayload {
    public static final Identifier ID = GraphLibDebugRenderImpl.id("graph_update_bulk");

    public static GraphUpdateBulkPayload decode(PacketByteBuf buf) {
        PayloadHeader header = PayloadHeader.decode(buf);

        int graphCount = buf.readVarInt();
        List<PayloadGraph> graphs = new ObjectArrayList<>(graphCount);
        for (int i = 0; i < graphCount; i++) {
            graphs.add(PayloadGraph.decode(buf));
        }

        return new GraphUpdateBulkPayload(header, graphs);
    }

    @Override
    public void write(PacketByteBuf buf) {
        header.write(buf);

        buf.writeVarInt(graphs.size());
        for (PayloadGraph graph : graphs) {
            graph.write(buf);
        }
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
