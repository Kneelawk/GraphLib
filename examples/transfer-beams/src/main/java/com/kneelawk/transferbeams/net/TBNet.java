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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.NetIdData;
import alexiil.mc.lib.net.ParentNetId;
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil;
import alexiil.mc.lib.net.impl.McNetworkStack;

import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.api.util.PacketEncodingUtil;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.item.LinkToolItem;

public class TBNet {
    public static void init() {
        // performs static initialization
    }

    public static final ParentNetId TRANSFER_BEAMS = McNetworkStack.ROOT.child(TransferBeamsMod.MOD_ID);
    public static final NetIdData NODE_REMOVE =
        TRANSFER_BEAMS.idData("node_remove").toServerOnly().setReceiver(TBNet::receiveNodeRemove);
    public static final NetIdData NODE_LINK =
        TRANSFER_BEAMS.idData("node_link").toServerOnly().setReceiver(TBNet::receiveNodeLink);

    public static void sendNodeRemove(NodePos pos) {
        NODE_REMOVE.send(CoreMinecraftNetUtil.getClientConnection(),
            (buf, ctx) -> PacketEncodingUtil.encodeNodePos(pos, buf, ctx, TransferBeamsMod.SYNCED));
    }

    private static void receiveNodeRemove(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        NodePos pos = PacketEncodingUtil.decodeNodePos(buf, ctx, TransferBeamsMod.SYNCED);

        PlayerEntity player = ctx.getConnection().getPlayer();

        // make sure the player is reasonably close
        if (pos.pos().getSquaredDistanceToCenter(player.getPos()) > 100.0) return;

        World playerWorld = player.getWorld();
        if (!(playerWorld instanceof ServerWorld serverWorld)) return;

        // FIXME: no claim detection

        GraphWorld world = TransferBeamsMod.UNIVERSE.getServerGraphWorld(serverWorld);

        // TODO: drop items properly into player inventory
        world.removeBlockNode(pos);
    }

    public static void sendNodeLink(NodePos pos) {
        NODE_LINK.send(CoreMinecraftNetUtil.getClientConnection(),
            (buf, ctx) -> PacketEncodingUtil.encodeNodePos(pos, buf, ctx, TransferBeamsMod.SYNCED));
    }

    private static void receiveNodeLink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        NodePos pos = PacketEncodingUtil.decodeNodePos(buf, ctx, TransferBeamsMod.SYNCED);

        PlayerEntity player = ctx.getConnection().getPlayer();

        // make sure the player is reasonably close
        if (pos.pos().getSquaredDistanceToCenter(player.getPos()) > 100.0) return;

        World playerWorld = player.getWorld();
        if (!(playerWorld instanceof ServerWorld serverWorld)) return;

        // FIXME: no claim detection

        GraphWorld world = TransferBeamsMod.UNIVERSE.getServerGraphWorld(serverWorld);

        LinkToolItem.onNodeClick(player, world, pos);
    }
}
