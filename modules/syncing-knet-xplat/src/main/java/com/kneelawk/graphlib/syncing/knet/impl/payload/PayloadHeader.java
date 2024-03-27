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

import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;

public record PayloadHeader(Identifier universeId, Palette<Identifier> palette, NetByteBuf data) {
    public static PayloadHeader decode(NetByteBuf buf) {
        Identifier universeId = buf.readIdentifier();
        Palette<Identifier> palette = Palette.decode(buf, NetByteBuf::readIdentifier);
        int dataLen = buf.readVarUnsignedInt();
        NetByteBuf data = NetByteBuf.buffer(dataLen);
        buf.readBytes(data, dataLen);

        return new PayloadHeader(universeId, palette, data);
    }
    
    public void encode(NetByteBuf buf) {
        buf.writeIdentifier(universeId);
        palette.encode(buf, NetByteBuf::writeIdentifier);
        buf.writeBytes(data, data.readerIndex(), data.readableBytes());
    }
}
