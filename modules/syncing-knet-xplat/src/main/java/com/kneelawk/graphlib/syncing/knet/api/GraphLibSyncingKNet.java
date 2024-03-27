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

package com.kneelawk.graphlib.syncing.knet.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.api.GraphLibSyncing;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.LinkPosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.NodePosSmallPayload;
import com.kneelawk.graphlib.syncing.knet.api.util.UniversePayload;
import com.kneelawk.knet.api.channel.context.ChannelContext;
import com.kneelawk.knet.api.channel.context.ChildChannelContext;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.channel.context.RootChannelContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;

/**
 * KNet-based synchronization library.
 */
public final class GraphLibSyncingKNet {
    private GraphLibSyncingKNet() {}

    /**
     * Channel context for referencing a specific universe.
     */
    public static final ChannelContext<KNetSyncedUniverse> UNIVERSE_CONTEXT =
        new RootChannelContext<>(UniversePayload.CODEC, (payload, ctx) -> decodeUniverse(payload),
            GraphLibSyncingKNet::encodeUniverse);

    /**
     * Channel context for referencing a node entity.
     */
    public static final ChannelContext<NodeEntity> NODE_ENTITY_CONTEXT =
        new ChildChannelContext<>(UNIVERSE_CONTEXT, NodePosPayload.CODEC, (universe, nodePosPayload, ctx) -> {
            NodePos pos = decodeNodePos(nodePosPayload, universe);

            GraphView world = universe.getSidedGraphView(ctx.mustGetWorld());
            if (world == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetWorld());

            NodeEntity entity = world.getNodeEntity(pos);
            if (entity == null) throw new PayloadHandlingErrorException("No node entity present at: " + pos);

            return entity;
        }, context -> {
            NodePos pos = context.getContext().getPos();

            KNetSyncedUniverse knet = getUniverse(context.getContext().getGraphWorld());

            return encodeNodePos(pos, knet);
        }, nodeEntity -> getUniverse(nodeEntity.getContext().getGraphWorld()));

    /**
     * Channel context for referencing a link entity.
     */
    public static final ChannelContext<LinkEntity> LINK_ENTITY_CONTEXT =
        new ChildChannelContext<>(UNIVERSE_CONTEXT, LinkPosPayload.CODEC, (universe, linkPosPayload, ctx) -> {
            LinkPos pos = decodeLinkPos(linkPosPayload, universe);

            GraphView world = universe.getSidedGraphView(ctx.mustGetWorld());
            if (world == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetWorld());

            LinkEntity entity = world.getLinkEntity(pos);
            if (entity == null) throw new PayloadHandlingErrorException("No link entity present at: " + pos);

            return entity;
        }, context -> {
            LinkPos pos = context.getContext().getPos();

            KNetSyncedUniverse knet = getUniverse(context.getContext().getGraphWorld());

            return encodeLinkPos(pos, knet);
        }, linkEntity -> getUniverse(linkEntity.getContext().getGraphWorld()));

    /**
     * Channel context for referencing a graph entity.
     */
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
            graphEntity -> getUniverse(graphEntity.getContext().getGraphWorld()));

    /**
     * Syncing for {@link EmptyLinkKey}.
     */
    public static final LinkKeySyncing EMPTY_KEY_SYNCING = LinkKeySyncing.ofNoOp(() -> EmptyLinkKey.INSTANCE);

    /**
     * Gets a KNet synced universe with the given universe id.
     *
     * @param universeId the id of the universe to get the KNet synced universe for.
     * @return the KNet synced universe with the given universe id.
     */
    public static @NotNull KNetSyncedUniverse getUniverse(@NotNull Identifier universeId) {
        SyncedUniverse universe = GraphLibSyncing.getUniverse(universeId);
        if (!(universe instanceof KNetSyncedUniverse knet)) throw new IllegalArgumentException(
            "Given universe " + universeId + " is not a KNetSyncedUniverse but is instead a " + universe.getClass());
        return knet;
    }

    /**
     * Gets a KNet synced universe associated with the given universe.
     *
     * @param universe the universe to get the KNet synced universe of.
     * @return the KNet synced universe associated with the given universe.
     */
    public static @NotNull KNetSyncedUniverse getUniverse(@NotNull GraphUniverse universe) {
        return getUniverse(universe.getId());
    }

    /**
     * Gets the KNet synced universe associated with the given graph view's universe.
     *
     * @param view the graph view to get the KNet synced universe associated with.
     * @return the KNet synced universe associated with the given graph view's universe.
     */
    public static @NotNull KNetSyncedUniverse getUniverse(@NotNull GraphView view) {
        return getUniverse(view.getUniverse());
    }

    /**
     * Encodes a reference to a synced universe.
     *
     * @param universe the universe to encode a reference to.
     * @return the payload holding a reference to the given universe.
     */
    public static @NotNull UniversePayload encodeUniverse(@NotNull KNetSyncedUniverse universe) {
        return new UniversePayload(universe.getId());
    }

    /**
     * Decodes a reference to a synced universe.
     *
     * @param payload the payload holding a reference a synced universe.
     * @return the referenced synced universe.
     * @throws PayloadHandlingException if an error occurs while finding the referenced universe.
     */
    public static @NotNull KNetSyncedUniverse decodeUniverse(@NotNull UniversePayload payload)
        throws PayloadHandlingException {
        if (!GraphLibSyncing.syncingEnabled(payload.universeId()))
            throw new PayloadHandlingErrorException("No universe present with id: " + payload.universeId());
        SyncedUniverse universe = GraphLibSyncing.getUniverse(payload.universeId());
        if (!(universe instanceof KNetSyncedUniverse knet)) throw new PayloadHandlingErrorException(
            "Universe with id " + payload.universeId() + " is not a KNet universe");
        return knet;
    }

    /**
     * Encodes a {@link NodePos} into a payload.
     *
     * @param nodePos  the {@link NodePos} to encode.
     * @param universe the universe that the {@link NodePos} exists in.
     * @return the encoded payload.
     */
    public static @NotNull NodePosPayload encodeNodePos(@NotNull NodePos nodePos,
                                                        @NotNull KNetSyncedUniverse universe) {
        BlockNodeType type = nodePos.node().getType();

        NetByteBuf nodeBuf = NetByteBuf.buffer();
        universe.getNodeSyncing(type).encode(nodePos.node(), nodeBuf);

        return new NodePosPayload(nodePos.pos(), type.getId(), nodeBuf);
    }

    /**
     * Decodes a {@link NodePos} from a payload.
     *
     * @param payload  the payload to decode from.
     * @param universe the universe the {@link NodePos} exists in.
     * @return the decoded {@link NodePos}.
     * @throws PayloadHandlingException if an error occurs while decoding the payload.
     */
    public static @NotNull NodePos decodeNodePos(@NotNull NodePosPayload payload, @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        BlockNodeType type = universe.getUniverse().getNodeType(payload.typeId());
        if (type == null) throw new PayloadHandlingErrorException(
            "Invalid block node type: " + payload.typeId() + " @ " + payload.pos());
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);

        BlockNode node = syncing.decode(payload.nodeBuf());

        return new NodePos(payload.pos(), node);
    }

    /**
     * Encodes a {@link NodePos} into a payload, while allowing node data and palette data to go in a header.
     *
     * @param nodePos  the {@link NodePos} to encode.
     * @param nodeBuf  a buffer for node data to be written to, to go in the header.
     * @param palette  a palette to be filled out, to go in the header.
     * @param universe the universe the {@link NodePos} exists in.
     * @return the encoded payload.
     */
    public static @NotNull NodePosSmallPayload encodeNodePosSmall(@NotNull NodePos nodePos, @NotNull NetByteBuf nodeBuf,
                                                                  @NotNull Palette<Identifier> palette,
                                                                  @NotNull KNetSyncedUniverse universe) {
        BlockNodeType type = nodePos.node().getType();

        universe.getNodeSyncing(type).encode(nodePos.node(), nodeBuf);

        int typeInt = palette.keyFor(type.getId());

        return new NodePosSmallPayload(nodePos.pos(), typeInt);
    }

    /**
     * Decodes a {@link NodePos} from a payload, reading node data and palette data from a header.
     *
     * @param payload  the payload to read from.
     * @param nodeBuf  the buffer of node data read from the header.
     * @param palette  the palette read from the header.
     * @param universe the universe the {@link NodePos} exists in.
     * @return the decoded {@link NodePos}.
     * @throws PayloadHandlingException if an error occurs while decoding the payload.
     */
    public static @NotNull NodePos decodeNodePosSmall(@NotNull NodePosSmallPayload payload, @NotNull NetByteBuf nodeBuf,
                                                      @NotNull Palette<Identifier> palette,
                                                      @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        Identifier typeId = palette.get(payload.typeId());
        if (typeId == null) throw new PayloadHandlingErrorException(
            "Invalid block node type int: " + payload.typeId() + " @ " + payload.pos());
        BlockNodeType type = universe.getUniverse().getNodeType(typeId);
        if (type == null)
            throw new PayloadHandlingErrorException("Invalid block node type: " + typeId + " @ " + payload.pos());
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);

        BlockNode node = syncing.decode(nodeBuf);

        return new NodePos(payload.pos(), node);
    }

    /**
     * Encodes a {@link LinkPos} into a payload.
     *
     * @param linkPos  the {@link LinkPos} to encode.
     * @param universe the universe that the link exists in.
     * @return the encoded payload.
     */
    public static @NotNull LinkPosPayload encodeLinkPos(@NotNull LinkPos linkPos,
                                                        @NotNull KNetSyncedUniverse universe) {
        NodePosPayload first = encodeNodePos(linkPos.first(), universe);
        NodePosPayload second = encodeNodePos(linkPos.second(), universe);

        LinkKeyType type = linkPos.key().getType();

        NetByteBuf linkBuf = NetByteBuf.buffer();
        universe.getLinkKeySyncing(type).encode(linkPos.key(), linkBuf);

        return new LinkPosPayload(first, second, type.getId(), linkBuf);
    }

    /**
     * Decodes a {@link LinkPos} from a payload.
     *
     * @param payload  the payload to decode.
     * @param universe the universe the link exists in.
     * @return the decoded {@link LinkPos}.
     * @throws PayloadHandlingException if an error occurred while decoding the {@link LinkPos}.
     */
    public static @NotNull LinkPos decodeLinkPos(@NotNull LinkPosPayload payload, @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        NodePos first = decodeNodePos(payload.first(), universe);
        NodePos second = decodeNodePos(payload.second(), universe);

        LinkKeyType type = universe.getUniverse().getLinkKeyType(payload.typeId());
        if (type == null) throw new PayloadHandlingErrorException(
            "Invalid link key type: " + payload.typeId() + " @ " + first + "-" + second);
        LinkKeySyncing syncing = universe.getLinkKeySyncing(type);

        LinkKey linkKey = syncing.decode(payload.linkBuf());

        return new LinkPos(first, second, linkKey);
    }

    /**
     * Encodes a {@link LinkPos} into a payload, allowing node data, link key data, and palette data to go in a header.
     *
     * @param linkPos    the {@link LinkPos} to encode.
     * @param nodeBuf    the buffer that node data is written to.
     * @param linkKeyBuf the buffer that link key data is written to.
     * @param palette    the id palette.
     * @param universe   the universe the {@link LinkPos} exists in.
     * @return the encoded payload.
     */
    public static @NotNull LinkPosSmallPayload encodeLinkPosSmall(@NotNull LinkPos linkPos, @NotNull NetByteBuf nodeBuf,
                                                                  @NotNull NetByteBuf linkKeyBuf,
                                                                  @NotNull Palette<Identifier> palette,
                                                                  @NotNull KNetSyncedUniverse universe) {
        NodePosSmallPayload first = encodeNodePosSmall(linkPos.first(), nodeBuf, palette, universe);
        NodePosSmallPayload second = encodeNodePosSmall(linkPos.second(), nodeBuf, palette, universe);

        LinkKeyType type = linkPos.key().getType();

        universe.getLinkKeySyncing(type).encode(linkPos.key(), linkKeyBuf);

        int typeId = palette.keyFor(type.getId());

        return new LinkPosSmallPayload(first, second, typeId);
    }

    /**
     * Decodes a {@link LinkPos} from a paylaod, allowing node data, link key data, and palette data to be read from a
     * header.
     *
     * @param payload    the payload to decode.
     * @param nodeBuf    the buffer to read node data from.
     * @param linkKeyBuf the buffer to read link key data from.
     * @param palette    the palette to use to decode node and link key ids from integers.
     * @param universe   the universe the {@link LinkPos} exists within.
     * @return the decoded {@link LinkPos}.
     * @throws PayloadHandlingException if an error occurs while decoding the payload.
     */
    public static @NotNull LinkPos decodeLinkPosSmall(@NotNull LinkPosSmallPayload payload, @NotNull NetByteBuf nodeBuf,
                                                      @NotNull NetByteBuf linkKeyBuf,
                                                      @NotNull Palette<Identifier> palette,
                                                      @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        NodePos first = decodeNodePosSmall(payload.first(), nodeBuf, palette, universe);
        NodePos second = decodeNodePosSmall(payload.second(), nodeBuf, palette, universe);

        Identifier typeId = palette.get(payload.typeId());
        if (typeId == null) throw new PayloadHandlingErrorException(
            "Invalid link key type int: " + payload.typeId() + " @ " + first + "-" + second);
        LinkKeyType type = universe.getUniverse().getLinkKeyType(typeId);
        if (type == null)
            throw new PayloadHandlingErrorException("Invalid link key type: " + typeId + " @ " + first + "-" + second);
        LinkKeySyncing syncing = universe.getLinkKeySyncing(type);

        LinkKey key = syncing.decode(linkKeyBuf);

        return new LinkPos(first, second, key);
    }

    private record GraphEntityPayload(long graphId, Identifier typeId) {
        public static final PayloadCodec<GraphEntityPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
            buf.writeVarUnsignedLong(payload.graphId);
            buf.writeIdentifier(payload.typeId);
        }, buf -> {
            long graphId = buf.readVarUnsignedLong();
            Identifier typeId = buf.readIdentifier();
            return new GraphEntityPayload(graphId, typeId);
        });
    }
}