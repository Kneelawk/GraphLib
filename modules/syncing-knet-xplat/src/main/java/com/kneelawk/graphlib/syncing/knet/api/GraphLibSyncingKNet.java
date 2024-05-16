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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.codextra.api.attach.stream.ChildBufferFactory;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.api.GraphLibSyncing;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.util.InSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.impl.StreamCodecHelper;
import com.kneelawk.knet.api.channel.context.PlayChannelContext;
import com.kneelawk.knet.api.channel.context.RootPlayChannelContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.util.NetBuf;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.Palette;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * KNet-based synchronization library.
 */
public final class GraphLibSyncingKNet {
    private GraphLibSyncingKNet() {}

    /**
     * Attachment key for a palette of {@link ResourceLocation}s.
     */
    public static final AttachmentKey<Palette<ResourceLocation>> ID_PALETTE = AttachmentKey.ofStaticFieldName();

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding.
     * <p>
     * This provides the {@link #ID_PALETTE} attachment.
     *
     * @param wrappedCodec    the codec to wrap.
     * @param childBufferCtor the constructor for the buffer type the wrapped codec uses.
     * @param <B1>            the type of the parent buffer.
     * @param <B2>            the type of the child buffer.
     * @param <V>             the result type.
     * @return the wrapper stream codec.
     */
    public static <B1 extends FriendlyByteBuf & NetBuf<B1>, B2 extends FriendlyByteBuf, V> StreamCodec<B1, V> attachPalette(
        StreamCodec<? super B2, V> wrappedCodec, ChildBufferFactory<? super B1, B2> childBufferCtor) {
        return ID_PALETTE.mutReadAttachingStreamCodec(Palette.codec(ResourceLocation.STREAM_CODEC), childBufferCtor,
            wrappedCodec, obj -> new Palette<>());
    }

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding, using a
     * buffer capable of being used as a {@link net.minecraft.network.RegistryFriendlyByteBuf}.
     * <p>
     * This provides the {@link #ID_PALETTE} attachment.
     *
     * @param wrappedCodec the codec to wrap.
     * @param <V>          the result type.
     * @return the wrapper stream codec.
     */
    public static <V> StreamCodec<NetRegistryByteBuf, V> registryAttachPalette(
        StreamCodec<? super NetRegistryByteBuf, V> wrappedCodec) {
        return attachPalette(wrappedCodec, (cap, old) -> NetBufs.netRegistryBuf(cap, old.registryAccess()));
    }

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding, using a
     * buffer capable of being used as a {@link NetByteBuf}.
     * <p>
     * This provides the {@link #ID_PALETTE} attachment.
     *
     * @param wrappedCodec the codec to wrap.
     * @param <V>          the result type.
     * @return the wrapper stream codec.
     */
    public static <V> StreamCodec<NetRegistryByteBuf, V> netAttachPalette(
        StreamCodec<? super RegistryNetByteBuf, V> wrappedCodec) {
        return attachPalette(wrappedCodec, (cap, old) -> NetBufs.registryNetBuf(cap, old.registryAccess()));
    }

    /**
     * Stream codec that encodes/decodes an entire {@link BlockNode}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, BlockNode> BLOCK_NODE_CODEC =
        StreamCodecHelper.createObjStreamCodec(BlockNodeSyncing.REF_CODEC, BlockNode::getType,
            KNetSyncedUniverse::getNodeSyncing, BlockNodeSyncing::getCodec, "BlockNode");

    /**
     * Stream codec that encodes/decodes an entire {@link LinkKey}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, LinkKey> LINK_KEY_CODEC =
        StreamCodecHelper.createObjStreamCodec(LinkKeySyncing.REF_CODEC, LinkKey::getType,
            KNetSyncedUniverse::getLinkKeySyncing, LinkKeySyncing::getCodec, "LinkKey");

    /**
     * Stream codec that encodes/decodes an entire {@link NodeEntity}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, NodeEntity> NODE_ENTITY_CODEC =
        StreamCodecHelper.createObjStreamCodec(NodeEntitySyncing.REF_CODEC, NodeEntity::getType,
            KNetSyncedUniverse::getNodeEntitySyncing, NodeEntitySyncing::getCodec, "NodeEntity");

    /**
     * Stream codec that encodes/decodes an entire {@link LinkEntity}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, LinkEntity> LINK_ENTITY_CODEC =
        StreamCodecHelper.createObjStreamCodec(LinkEntitySyncing.REF_CODEC, LinkEntity::getType,
            KNetSyncedUniverse::getLinkEntitySyncing, LinkEntitySyncing::getCodec, "LinkEntity");

    /**
     * Stream codec that encodes/decodes an entire {@link GraphEntity}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, GraphEntity<?>> GRAPH_ENTITY_CODEC =
        StreamCodecHelper.createObjStreamCodec(GraphEntitySyncing.REF_CODEC, GraphEntity::getType,
            KNetSyncedUniverse::getGraphEntitySyncing, GraphEntitySyncing::getCodec, "GraphEntity");

    /**
     * Stream codec for a {@link NodePos}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, NodePos> NODE_POS_CODEC =
        StreamCodec.composite(NetCodecs.BLOCK_POS.mapStream(NetBufs::netOf), NodePos::pos, BLOCK_NODE_CODEC,
            NodePos::node, NodePos::new);

    /**
     * Stream codec for a {@link LinkPos}.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE} attachment.
     */
    public static final StreamCodec<NetRegistryByteBuf, LinkPos> LINK_POS_CODEC =
        StreamCodec.composite(NODE_POS_CODEC, LinkPos::first, NODE_POS_CODEC, LinkPos::second, LINK_KEY_CODEC,
            LinkPos::key, LinkPos::new);

    /**
     * Stream codec for a {@link NodePos} with universe attached.
     *
     * @param universe the universe to attach.
     * @return the codec with universe attached.
     */
    public static StreamCodec<NetRegistryByteBuf, NodePos> nodePosCodec(KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, NODE_POS_CODEC);
    }

    /**
     * Stream codec for a {@link LinkPos} with universe attached.
     *
     * @param universe the universe to attach.
     * @return the codec with universe attached.
     */
    public static StreamCodec<NetRegistryByteBuf, LinkPos> linkPosCodec(KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, LINK_POS_CODEC);
    }

    /**
     * Channel context for referencing a node entity.
     */
    public static final PlayChannelContext<NodeEntity> NODE_ENTITY_CONTEXT =
        RootPlayChannelContext.ofRegistryCodec(InSyncedUniverse.codec(NODE_POS_CODEC), (payload, ctx) -> {
            GraphView view = payload.universe().getSidedGraphView(ctx.mustGetLevel());
            if (view == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetLevel());

            NodeEntity entity = view.getNodeEntity(payload.obj());
            if (entity == null) throw new PayloadHandlingErrorException("No node entity present at: " + payload.obj());

            return entity;
        }, entity -> new InSyncedUniverse<>(getUniverse(entity.getContext().getGraphWorld()),
            entity.getContext().getPos()));

    /**
     * Channel context for referencing a link entity.
     */
    public static final PlayChannelContext<LinkEntity> LINK_ENTITY_CONTEXT =
        RootPlayChannelContext.ofRegistryCodec(InSyncedUniverse.codec(LINK_POS_CODEC), (payload, ctx) -> {
            GraphView view = payload.universe().getSidedGraphView(ctx.mustGetLevel());
            if (view == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetLevel());

            LinkEntity entity = view.getLinkEntity(payload.obj());
            if (entity == null) throw new PayloadHandlingErrorException("No link entity present at " + payload.obj());

            return entity;
        }, entity -> new InSyncedUniverse<>(getUniverse(entity.getContext().getGraphWorld()),
            entity.getContext().getPos()));

    /**
     * Channel context for referencing a graph entity.
     */
    public static final PlayChannelContext<GraphEntity<?>> GRAPH_ENTITY_CONTEXT =
        RootPlayChannelContext.ofNetCodec(InSyncedUniverse.codec(GraphEntityPayload.CODEC), (payload, ctx) -> {
            GraphView view = payload.universe().getSidedGraphView(ctx.mustGetLevel());
            if (view == null) throw new PayloadHandlingErrorException(
                "Unable to get the graph view associated with: " + ctx.mustGetLevel());

            BlockGraph graph = view.getGraph(payload.obj().graphId());
            if (graph == null) throw new PayloadHandlingErrorException("No graph with id: " + payload.obj().graphId());

            return graph.getGraphEntity(payload.obj().syncing().getType());
        }, entity -> {
            KNetSyncedUniverse universe = getUniverse(entity.getContext().getGraphWorld());
            return new InSyncedUniverse<>(universe, new GraphEntityPayload(entity.getContext().getGraph().getId(),
                universe.getGraphEntitySyncing(entity.getType())));
        });

    /**
     * Syncing for {@link EmptyLinkKey}.
     */
    public static final LinkKeySyncing EMPTY_KEY_SYNCING =
        LinkKeySyncing.ofNoOp(EmptyLinkKey.TYPE, () -> EmptyLinkKey.INSTANCE);

    /**
     * Gets a KNet synced universe with the given universe id.
     *
     * @param universeId the id of the universe to get the KNet synced universe for.
     * @return the KNet synced universe with the given universe id.
     */
    public static @NotNull KNetSyncedUniverse getUniverse(@NotNull ResourceLocation universeId) {
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

    private record GraphEntityPayload(long graphId, GraphEntitySyncing<?> syncing) {
        public static final StreamCodec<NetByteBuf, GraphEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, GraphEntityPayload::graphId,
            GraphEntitySyncing.REF_CODEC, GraphEntityPayload::syncing,
            GraphEntityPayload::new
        );
    }
}
