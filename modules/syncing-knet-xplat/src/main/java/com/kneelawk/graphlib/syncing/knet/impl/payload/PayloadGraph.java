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

import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.syncing.knet.api.SyncingKNet;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

public record PayloadGraph(long graphId, List<GraphEntity<?>> entities, List<PayloadNode> nodes,
                           List<PayloadInternalLink> internalLinks, List<PayloadExternalLink> externalLinks) {
    public static final StreamCodec<NetRegistryByteBuf, PayloadGraph> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, PayloadGraph::graphId,
        SyncingKNet.GRAPH_ENTITY_CODEC.apply(ByteBufCodecs.list()), PayloadGraph::entities,
        PayloadNode.CODEC.apply(ByteBufCodecs.list()), PayloadGraph::nodes,
        PayloadInternalLink.CODEC.apply(ByteBufCodecs.list()), PayloadGraph::internalLinks,
        PayloadExternalLink.CODEC.apply(ByteBufCodecs.list()), PayloadGraph::externalLinks,
        PayloadGraph::new
    );

    public void discard() {
        entities.forEach(GraphEntity::onDiscard);
        nodes.forEach(node -> node.entity().ifPresent(NodeEntity::onDiscard));
        internalLinks.forEach(link -> link.entity().ifPresent(LinkEntity::onDiscard));
        externalLinks.forEach(link -> link.entity().ifPresent(LinkEntity::onDiscard));
    }
}
