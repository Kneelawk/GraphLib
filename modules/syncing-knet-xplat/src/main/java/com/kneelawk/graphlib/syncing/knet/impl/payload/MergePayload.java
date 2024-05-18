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

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.kneelawk.codextra.api.util.FunctionUtils;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.impl.SyncingKNetImpl;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

public record MergePayload(KNetSyncedUniverse universe, long fromId, long intoId,
                           List<GraphEntity<?>> intoGraphEntities)
    implements CustomPacketPayload {
    public static final Type<MergePayload> ID = new Type<>(SyncingKNetImpl.id("merge"));
    public static final StreamCodec<NetRegistryByteBuf, MergePayload> CODEC = StreamCodec.composite(
        KNetSyncedUniverse.ATTACHMENT_KEY.retrieveStream(), FunctionUtils.nullFunc(),
        ByteBufCodecs.VAR_LONG, MergePayload::fromId,
        ByteBufCodecs.VAR_LONG, MergePayload::intoId,
        GraphLibSyncingKNet.GRAPH_ENTITY_CODEC.apply(ByteBufCodecs.list()), MergePayload::intoGraphEntities,
        MergePayload::new
    ).apply(KNetSyncedUniverse.readAttachingOp(MergePayload::universe));

    @Override
    public Type<?> type() {
        return ID;
    }
}
