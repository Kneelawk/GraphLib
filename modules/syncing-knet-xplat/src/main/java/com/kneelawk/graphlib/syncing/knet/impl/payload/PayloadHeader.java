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

import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;
import net.minecraft.resources.ResourceLocation;

public record PayloadHeader(ResourceLocation universeId, Palette<ResourceLocation> palette, NetByteBuf data) {
    public static PayloadHeader decode(NetByteBuf buf) {
        ResourceLocation universeId = buf.readResourceLocation();
        Palette<ResourceLocation> palette = Palette.decode(buf, NetByteBuf::readResourceLocation);
        int dataLen = buf.readVarUnsignedInt();
        NetByteBuf data = NetBufs.netBuf(dataLen);
        buf.readBytes(data, dataLen);

        return new PayloadHeader(universeId, palette, data);
    }
    
    public void encode(NetByteBuf buf) {
        buf.writeResourceLocation(universeId);
        palette.encode(buf, NetByteBuf::writeResourceLocation);
        buf.writeVarUnsignedInt(data.readableBytes());
        buf.writeBytes(data, data.readerIndex(), data.readableBytes());
    }
}
