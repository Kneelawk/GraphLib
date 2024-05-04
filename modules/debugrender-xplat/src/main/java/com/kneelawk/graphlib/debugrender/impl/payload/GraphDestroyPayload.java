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

import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GraphDestroyPayload(ResourceLocation universeId, long graphId) implements CustomPacketPayload {
    public static final Type<GraphDestroyPayload> ID = new Type<>(GraphLibDebugRenderImpl.id("graph_destroy"));
    public static final StreamCodec<FriendlyByteBuf, GraphDestroyPayload> CODEC =
        StreamCodec.ofMember(GraphDestroyPayload::write, GraphDestroyPayload::new);

    public GraphDestroyPayload(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readLong());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(universeId);
        buf.writeLong(graphId);
    }

    @Override
    public Type<?> type() {
        return ID;
    }
}
