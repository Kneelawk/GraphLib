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

package com.kneelawk.graphlib.debugrender.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;

/**
 * Used for decoding a {@link DebugBlockNode} from a {@link PacketByteBuf}.
 */
public interface BlockNodeDebugPacketDecoder {
    /**
     * Decodes a {@link DebugBlockNode} from a {@link PacketByteBuf}.
     *
     * @param buf a buffer for reading the data written by
     *            {@link BlockNodeDebugPacketEncoder#encode(BlockNode, NodeHolder, PacketByteBuf)}.
     *            Note: this buffer will contain other data besides this node's data.
     * @return a {@link DebugBlockNode} containing the data decoded from the {@link PacketByteBuf}.
     */
    @Nullable DebugBlockNode fromPacket(@NotNull PacketByteBuf buf);
}
