/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
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

package com.kneelawk.graphlib.debugrender.api.graph;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.debugrender.api.client.BlockNodeDebugPacketDecoder;
import com.kneelawk.graphlib.debugrender.api.client.GraphLibDebugRenderClient;

/**
 * Encodes a {@link BlockNode} for sending to the client-side debug renderer.
 */
public interface BlockNodeDebugPacketEncoder {
    /**
     * Encodes a {@link BlockNode} for sending to  the client-side debug renderer.
     * <p>
     * The default implementations of this method are compatible with the default client-side block node decoders.
     * This interface does <b>not</b> need to be implemented in order for client-side graph debug rendering to work.
     * This interface should only be implemented to provide custom data to the client.
     * <p>
     * If custom data is being sent to the client, use
     * {@link GraphLibDebugRenderClient#registerDebugDecoder(Identifier, Identifier, BlockNodeDebugPacketDecoder)}
     * to register a decoder for the custom data.
     *
     * @param node the node to encode.
     * @param self the node holder for context.
     * @param buf  the buffer to write to.
     */
    void encode(BlockNode node, NodeHolder<BlockNode> self, PacketByteBuf buf);
}
