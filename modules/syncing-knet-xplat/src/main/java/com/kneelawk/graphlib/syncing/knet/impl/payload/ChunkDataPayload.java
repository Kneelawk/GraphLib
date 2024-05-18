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

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

import com.kneelawk.codextra.api.util.FunctionUtils;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

import static com.kneelawk.graphlib.syncing.knet.impl.SyncingKNetImpl.id;

public record ChunkDataPayload(KNetSyncedUniverse universe, ChunkPos chunkPos, List<PayloadGraph> graphs)
    implements CustomPacketPayload {
    public static final Type<ChunkDataPayload> ID = new Type<>(id("chunk_data"));
    public static final StreamCodec<NetRegistryByteBuf, ChunkDataPayload> CODEC = StreamCodec.composite(
            KNetSyncedUniverse.ATTACHMENT_KEY.retrieveStream(), FunctionUtils.nullFunc(),
            NetCodecs.CHUNK_POS.mapStream(NetBufs::netOf), ChunkDataPayload::chunkPos,
            PayloadGraph.CODEC.apply(ByteBufCodecs.list()), ChunkDataPayload::graphs,
            ChunkDataPayload::new
        ).apply(KNetSyncedUniverse.readAttachingOp(ChunkDataPayload::universe))
        .apply(GraphLibSyncingKNet::registryAttachPalette);

    public void discard() {
        graphs.forEach(graph -> graph.discard());
    }

    @Override
    public @NotNull Type<?> type() {
        return ID;
    }
}
