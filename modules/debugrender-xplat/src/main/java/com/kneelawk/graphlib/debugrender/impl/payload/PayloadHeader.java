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

import io.netty.buffer.Unpooled;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record PayloadHeader(Identifier universeId, Int2ObjectMap<Identifier> palette, PacketByteBuf nodeData) {
    public static PayloadHeader decode(PacketByteBuf buf) {
        Identifier universeId = buf.readIdentifier();

        int paletteLen = buf.readVarInt();
        Int2ObjectMap<Identifier> palette = new Int2ObjectLinkedOpenHashMap<>();
        for (int i = 0; i < paletteLen; i++) {
            int key = buf.readVarInt();
            Identifier value = buf.readIdentifier();
            palette.put(key, value);
        }

        int nodeDataLen = buf.readInt();
        PacketByteBuf nodeData = new PacketByteBuf(Unpooled.buffer(nodeDataLen));
        buf.readBytes(nodeData, nodeDataLen);

        return new PayloadHeader(universeId, palette, nodeData);
    }

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(universeId);

        buf.writeVarInt(palette.size());
        for (Int2ObjectMap.Entry<Identifier> entry : palette.int2ObjectEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            buf.writeIdentifier(entry.getValue());
        }

        buf.writeInt(nodeData.readableBytes());
        buf.writeBytes(nodeData, nodeData.readerIndex(), nodeData.readableBytes());
    }
}
