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

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.api.GraphLibSyncing;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.PacketEncodingUtil;
import com.kneelawk.graphlib.syncing.knet.api.util.UniversePayload;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.channel.context.ChannelContext;
import com.kneelawk.knet.api.channel.context.ChildChannelContext;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.channel.context.RootChannelContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;

public final class KNetChannels {
    private KNetChannels() {}

    public static void register(KNetRegistrar registrar) {
    }

    public static final ChannelContext<KNetSyncedUniverse> UNIVERSE_CONTEXT =
        new RootChannelContext<>(UniversePayload.CODEC, (payload, ctx) -> PacketEncodingUtil.decodeUniverse(payload),
            PacketEncodingUtil::encodeUniverse);

    public static final ChannelContext<NodeEntity> NODE_ENTITY_CONTEXT =
        new ChildChannelContext<>(UNIVERSE_CONTEXT, NodePosPayload.CODEC, (universe, nodePosPayload, ctx) -> {
            NodePos pos = PacketEncodingUtil.decodeNodePos(nodePosPayload, universe);

            GraphView world = universe.getSidedGraphView(ctx.mustGetWorld());
            if (world == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetWorld());

            NodeEntity entity = world.getNodeEntity(pos);
            if (entity == null) throw new PayloadHandlingErrorException("No node entity present at: " + pos);

            return entity;
        }, context -> {
            NodePos pos = context.getContext().getPos();

            SyncedUniverse universe = GraphLibSyncing.getUniverse(context.getContext().getGraphWorld());
            if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
                "Unable to encode a NodeEntity not associated with a KNetSyncedUniverse. Found instead: " + universe);

            return PacketEncodingUtil.encodeNodePos(pos, knet);
        }, nodeEntity -> {
            SyncedUniverse universe = GraphLibSyncing.getUniverse(nodeEntity.getContext().getGraphWorld());
            if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
                "Unable to encode a NodeEntity not associated with a KNetSyncedUniverse. Found instead: " + universe);
            return knet;
        });

    public static final ChannelContext<LinkEntity> LINK_ENTITY_CONTEXT =
        new ChildChannelContext<>(UNIVERSE_CONTEXT, LinkPosPayload.CODEC, (universe, linkPosPayload, ctx) -> {
            LinkPos pos = PacketEncodingUtil.decodeLinkPos(linkPosPayload, universe);

            GraphView world = universe.getSidedGraphView(ctx.mustGetWorld());
            if (world == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetWorld());

            LinkEntity entity = world.getLinkEntity(pos);
            if (entity == null) throw new PayloadHandlingErrorException("No link entity present at: " + pos);

            return entity;
        }, context -> {
            LinkPos pos = context.getContext().getPos();

            SyncedUniverse universe = GraphLibSyncing.getUniverse(context.getContext().getGraphWorld());
            if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
                "Unable to encode a NodeEntity not associated with a KNetSyncedUniverse. Found instead: " + universe);

            return PacketEncodingUtil.encodeLinkPos(pos, knet);
        }, linkEntity -> {
            SyncedUniverse universe = GraphLibSyncing.getUniverse(linkEntity.getContext().getGraphWorld());
            if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
                "Unable to encode a NodeEntity not associated with a KNetSyncedUniverse. Found instead: " + universe);
            return knet;
        });

    public record GraphEntityPayload(long graphId, Identifier typeId) {
        public static final PayloadCodec<GraphEntityPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
            buf.writeVarUnsignedLong(payload.graphId);
            buf.writeIdentifier(payload.typeId);
        }, buf -> {
            long graphId = buf.readVarUnsignedLong();
            Identifier typeId = buf.readIdentifier();
            return new GraphEntityPayload(graphId, typeId);
        });
    }

    public static final ChannelContext<GraphEntity<?>> GRAPH_ENTITY_CONTEXT =
        new ChildChannelContext<>(UNIVERSE_CONTEXT, GraphEntityPayload.CODEC, (universe, payload, ctx) -> {
            GraphView world = universe.getSidedGraphView(ctx.mustGetWorld());
            if (world == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetWorld());

            BlockGraph graph = world.getGraph(payload.graphId());
            if (graph == null) throw new PayloadHandlingErrorException("No graph with id: " + payload.graphId());

            GraphEntityType<?> type = universe.getUniverse().getGraphEntityType(payload.typeId());
            if (type == null)
                throw new PayloadHandlingErrorException("No graph entity type with id: " + payload.typeId());

            return graph.getGraphEntity(type);
        }, context -> new GraphEntityPayload(context.getContext().getGraph().getId(), context.getType().getId()),
            graphEntity -> {
                SyncedUniverse universe = GraphLibSyncing.getUniverse(graphEntity.getContext().getGraphWorld());
                if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
                    "Unable to encode a NodeEntity not associated with a KNetSyncedUniverse. Found instead: " +
                        universe);
                return knet;
            });
}
