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

package com.kneelawk.graphlib.syncing.knet.impl;

import com.kneelawk.graphlib.syncing.knet.impl.payload.ChunkDataPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.LinkPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.MergePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeAddPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.NodeRemovePayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.SplitPayload;
import com.kneelawk.graphlib.syncing.knet.impl.payload.UnlinkPayload;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.channel.NoContextChannel;

public final class KNetChannels {
    private KNetChannels() {}

    public static void register(KNetRegistrar registrar) {
        registrar.register(CHUNK_DATA);
        registrar.register(NODE_ADD);
        registrar.register(MERGE);
        registrar.register(LINK);
        registrar.register(UNLINK);
        registrar.register(SPLIT);
        registrar.register(NODE_REMOVE);
    }

    public static final NoContextChannel<ChunkDataPayload> CHUNK_DATA =
        new NoContextChannel<>(SyncingKNetImpl.id("chunk_data"), ChunkDataPayload::decode).recvClient(
            KNetDecoding::receiveChunkDataPacket);

    public static final NoContextChannel<NodeAddPayload> NODE_ADD =
        new NoContextChannel<>(SyncingKNetImpl.id("node_add"), NodeAddPayload::decode).recvClient(
            KNetDecoding::receiveNodeAdd);

    public static final NoContextChannel<MergePayload> MERGE =
        new NoContextChannel<>(SyncingKNetImpl.id("merge"), MergePayload::decode).recvClient(
            KNetDecoding::receiveMerge);

    public static final NoContextChannel<LinkPayload> LINK =
        new NoContextChannel<>(SyncingKNetImpl.id("link"), LinkPayload::decode).recvClient(KNetDecoding::receiveLink);

    public static final NoContextChannel<UnlinkPayload> UNLINK =
        new NoContextChannel<>(SyncingKNetImpl.id("unlink"), UnlinkPayload::decode).recvClient(
            KNetDecoding::receiveUnlink);

    public static final NoContextChannel<SplitPayload> SPLIT =
        new NoContextChannel<>(SyncingKNetImpl.id("split"), SplitPayload::decode).recvClient(
            KNetDecoding::receiveSplit);

    public static final NoContextChannel<NodeRemovePayload> NODE_REMOVE =
        new NoContextChannel<>(SyncingKNetImpl.id("node_remove"), NodeRemovePayload::decode).recvClient(
            KNetDecoding::receiveNodeRemove);
}
