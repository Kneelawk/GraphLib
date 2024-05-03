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

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosPayload;
import com.kneelawk.graphlib.syncing.knet.impl.SyncingKNetImpl;
import com.kneelawk.knet.api.util.NetByteBuf;

public record UnlinkPayload(Identifier universeId, long graphId, LinkPosPayload linkPos) implements CustomPayload {
    public static final Id<UnlinkPayload> ID = new Id<>(SyncingKNetImpl.id("unlink"));
    public static final PacketCodec<NetByteBuf, UnlinkPayload> CODEC =
        PacketCodec.of(UnlinkPayload::encode, UnlinkPayload::decode);

    public static UnlinkPayload decode(NetByteBuf buf) {
        Identifier universeId = buf.readIdentifier();
        long graphId = buf.readVarUnsignedLong();
        LinkPosPayload linkPos = LinkPosPayload.decode(buf);
        return new UnlinkPayload(universeId, graphId, linkPos);
    }

    public void encode(NetByteBuf buf) {
        buf.writeIdentifier(universeId);
        buf.writeVarUnsignedLong(graphId);
        linkPos.encode(buf);
    }

    @Override
    public Id<?> getId() {
        return ID;
    }
}
