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

package com.kneelawk.graphlib.netutil.impl.mixin.api;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

/**
 * Super-interface for all minecraft network handlers.
 */
public interface MinecraftNetworkHandler extends PacketListener {
    /**
     * Gets the connection associated with this network handler.
     *
     * @return the connection associated with this network handler.
     */
    ClientConnection gl_getConnection();

    /**
     * Sends a packet to the underlying connection.
     *
     * @param packet the packet to send.
     */
    default void gl_sendPacket(Packet<?> packet) {
        gl_getConnection().send(packet);
    }

    /**
     * Sends a custom payload to the underlying connection.
     *
     * @param payload the payload to send.
     */
    default void gl_sendPayload(CustomPayload payload) {
        if (getSide() == NetworkSide.C2S) {
            gl_sendPacket(new CustomPayloadS2CPacket(payload));
        } else {
            gl_sendPacket(new CustomPayloadC2SPacket(payload));
        }
    }
}
