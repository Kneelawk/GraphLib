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

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * A payload representing a universe.
 *
 * @param universeId the id of the specified universe.
 */
public record UniversePayload(@NotNull Identifier universeId) {
    /**
     * This payload's codec.
     */
    public static final PacketCodec<NetByteBuf, UniversePayload> CODEC =
        PacketCodec.of(UniversePayload::encode, UniversePayload::decode);

    /**
     * Decodes a payload from the buffer.
     *
     * @param buf the buffer to decode from.
     * @return the decoded payload.
     */
    public static UniversePayload decode(NetByteBuf buf) {return new UniversePayload(buf.readIdentifier());}

    /**
     * Encodes this payload to the buffer.
     *
     * @param buf the buffer to encode to.
     */
    public void encode(NetByteBuf buf) {buf.writeIdentifier(universeId);}
}
