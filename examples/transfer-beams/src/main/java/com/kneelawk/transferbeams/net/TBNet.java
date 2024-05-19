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

package com.kneelawk.transferbeams.net;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.knet.api.SyncingKNet;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.channel.NoContextPlayChannel;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.TransferNodeEntity;
import com.kneelawk.transferbeams.item.LinkToolItem;
import com.kneelawk.transferbeams.util.PlayerDropHandler;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class TBNet {
    private static final NoContextPlayChannel<NodeActivatePayload> NODE_ACTIVATE =
        NoContextPlayChannel.ofRegistryCodec(NodeActivatePayload.TYPE, NodeActivatePayload.CODEC)
            .recvServer(TBNet::receiveNodeActivate);
    private static final NoContextPlayChannel<NodeRemovePayload> NODE_REMOVE =
        NoContextPlayChannel.ofRegistryCodec(NodeRemovePayload.TYPE, NodeRemovePayload.CODEC)
            .recvServer(TBNet::receiveNodeRemove);
    private static final NoContextPlayChannel<NodeLinkPayload> NODE_LINK =
        NoContextPlayChannel.ofRegistryCodec(NodeLinkPayload.TYPE, NodeLinkPayload.CODEC)
            .recvServer(TBNet::receiveNodeLink);

    public static void init(KNetRegistrar registrar) {
        registrar.register(NODE_ACTIVATE);
        registrar.register(NODE_REMOVE);
        registrar.register(NODE_LINK);
    }

    public static void sendNodeActivate(NodePos pos) {
        NODE_ACTIVATE.sendToServer(new NodeActivatePayload(pos));
    }

    private static void receiveNodeActivate(NodeActivatePayload payload,
                                            PlayPayloadHandlingContext ctx) {
        NodePos pos = payload.pos();

        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;

        // make sure the player is reasonably close
        if (pos.pos().distToCenterSqr(player.position()) > 100.0) return;

        ServerLevel serverWorld = player.serverLevel();

        // FIXME: no claim detection

        GraphWorld world = TransferBeamsMod.UNIVERSE.getGraphWorld(serverWorld);

        NodeHolder<BlockNode> holder = world.getNodeAt(pos);
        if (holder == null) return;

        // call activate
        TransferNodeEntity entity = holder.getNodeEntity(TransferNodeEntity.class);
        if (entity == null) return;
        entity.onActivate(player);
    }

    public static void sendNodeRemove(NodePos pos) {
        NODE_REMOVE.sendToServer(new NodeRemovePayload(pos));
    }

    private static void receiveNodeRemove(NodeRemovePayload payload, PlayPayloadHandlingContext ctx) {
        NodePos pos = payload.pos();

        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return;

        // make sure the player is reasonably close
        if (pos.pos().distToCenterSqr(player.position()) > 100.0) return;

        ServerLevel serverWorld = player.serverLevel();

        // FIXME: no claim detection

        GraphWorld world = TransferBeamsMod.UNIVERSE.getGraphWorld(serverWorld);

        NodeHolder<BlockNode> holder = world.getNodeAt(pos);
        if (holder == null) return;

        // drop items into player inventory
        TransferNodeEntity entity = holder.getNodeEntity(TransferNodeEntity.class);
        if (entity == null) return;
        entity.dropItems(new PlayerDropHandler(player));

        world.removeBlockNode(pos);
    }

    public static void sendNodeLink(NodePos pos) {
        NODE_LINK.sendToServer(new NodeLinkPayload(pos));
    }

    private static void receiveNodeLink(NodeLinkPayload payload, PlayPayloadHandlingContext ctx) {
        NodePos pos = payload.pos();

        Player player = ctx.mustGetPlayer();

        // make sure the player is reasonably close
        if (pos.pos().distToCenterSqr(player.position()) > 100.0) return;

        Level playerWorld = player.level();
        if (!(playerWorld instanceof ServerLevel serverWorld)) return;

        // FIXME: no claim detection

        GraphWorld world = TransferBeamsMod.UNIVERSE.getGraphWorld(serverWorld);

        LinkToolItem.onNodeClick(player, world, pos);
    }

    private record NodeActivatePayload(NodePos pos) implements CustomPacketPayload {
        public static final Type<NodeActivatePayload> TYPE = new Type<>(id("node_activate"));
        public static final StreamCodec<NetRegistryByteBuf, NodeActivatePayload> CODEC =
            SyncingKNet.nodePosCodec(TransferBeamsMod.SYNCED).map(
                NodeActivatePayload::new, NodeActivatePayload::pos);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private record NodeRemovePayload(NodePos pos) implements CustomPacketPayload {
        public static final Type<NodeRemovePayload> TYPE = new Type<>(id("node_remove"));
        public static final StreamCodec<NetRegistryByteBuf, NodeRemovePayload> CODEC =
            SyncingKNet.nodePosCodec(TransferBeamsMod.SYNCED)
                .map(NodeRemovePayload::new, NodeRemovePayload::pos);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private record NodeLinkPayload(NodePos pos) implements CustomPacketPayload {
        public static final Type<NodeLinkPayload> TYPE = new Type<>(id("node_link"));
        public static final StreamCodec<NetRegistryByteBuf, NodeLinkPayload> CODEC =
            SyncingKNet.nodePosCodec(TransferBeamsMod.SYNCED).map(NodeLinkPayload::new, NodeLinkPayload::pos);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
