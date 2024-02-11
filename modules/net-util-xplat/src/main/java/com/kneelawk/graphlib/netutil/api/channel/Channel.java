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

package com.kneelawk.graphlib.netutil.api.channel;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.PayloadContext;

public interface Channel<P extends CustomPayload> {
    /**
     * Gets this channel's id.
     *
     * @return this channel's id.
     */
    Identifier getId();

    /**
     * Gets this channel's payload reader.
     *
     * @return this channel's payload reader.
     */
    PacketByteBuf.Reader<P> getReader();

    /**
     * Called by net-util platform code when this channel receives a payload on the client-side.
     *
     * @param payload the payload received.
     * @param ctx     the context used for applying the payload.
     */
    void handleClientPayload(P payload, PayloadContext ctx);

    /**
     * Called by net-util platform code when this channel receives a payload on the server-side.
     *
     * @param payload the payload received.
     * @param ctx     the context used for applying the payload.
     */
    void handleServerPayload(P payload, PayloadContext ctx);
}
