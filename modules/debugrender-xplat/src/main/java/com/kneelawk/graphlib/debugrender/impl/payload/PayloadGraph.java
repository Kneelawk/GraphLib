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
import net.minecraft.util.math.BlockPos;

public record PayloadGraph(long graphId, List<PayloadNode> nodes, List<PayloadLink> links) {
    public static PayloadGraph decode(PacketByteBuf buf) {
        long graphId = buf.readLong();

        int nodeCount = buf.readVarInt();
        List<PayloadNode> nodes = new ObjectArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            int typeId = buf.readVarInt();
            BlockPos pos = buf.readBlockPos();
            nodes.add(new PayloadNode(typeId, pos));
        }

        int linkCount = buf.readVarInt();
        List<PayloadLink> links = new ObjectArrayList<>(linkCount);
        for (int i = 0; i < linkCount; i++) {
            int nodeA = buf.readVarInt();
            int nodeB = buf.readVarInt();
            links.add(new PayloadLink(nodeA, nodeB));
        }

        return new PayloadGraph(graphId, nodes, links);
    }

    public void write(PacketByteBuf buf) {
        buf.writeLong(graphId);

        buf.writeVarInt(nodes.size());
        for (PayloadNode node : nodes) {
            buf.writeVarInt(node.typeId());
            buf.writeBlockPos(node.pos());
        }

        buf.writeVarInt(links.size());
        for (PayloadLink link : links) {
            buf.writeVarInt(link.nodeA());
            buf.writeVarInt(link.nodeB());
        }
    }
}
