/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.syncing.lns.api.graph.LNSSyncedUniverse;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.lns.impl.LNSNetworking;

/**
 * Contains various utility methods for en/decoding various types to/from packets.
 */
public class PacketEncodingUtil {
    private PacketEncodingUtil() {}

    /**
     * Syncing for {@link EmptyLinkKey}.
     */
    public static final LinkKeySyncing EMPTY_KEY_SYNCING = LinkKeySyncing.ofNoOp(() -> EmptyLinkKey.INSTANCE);

    /**
     * Writes this NodePos to a packet.
     *
     * @param nodePos  the node-pos to be encoded.
     * @param buf      the buffer to write to.
     * @param ctx      the packet's message context.
     * @param universe the universe that the node-pos's node is associated with.
     */
    public static void encodeNodePos(@NotNull NodePos nodePos, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx,
                                     @NotNull LNSSyncedUniverse universe) {
        buf.writeBlockPos(nodePos.pos());
        buf.writeVarUnsignedInt(LNSNetworking.ID_CACHE.getId(ctx.getConnection(), nodePos.node().getType().getId()));
        universe.getNodeSyncing(nodePos.node().getType()).encode(nodePos.node(), buf, ctx);
    }

    /**
     * Decodes a NodePos from a packet.
     *
     * @param buf      the buffer to read from.
     * @param ctx      the packet's message context.
     * @param universe the universe that the block node's decoder is to be retrieved from.
     * @return a newly decoded NodePos.
     * @throws InvalidInputDataException if there was an error while decoding the node pos.
     */
    public static @NotNull NodePos decodeNodePos(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx ctx,
                                                 @NotNull LNSSyncedUniverse universe) throws InvalidInputDataException {
        BlockPos pos = buf.readBlockPos();

        int idInt = buf.readVarUnsignedInt();
        Identifier typeId = LNSNetworking.ID_CACHE.getObj(ctx.getConnection(), idInt);
        if (typeId == null) {
            GLLog.warn("Unable to decode block node type id from unknown identifier int {} @ {}", idInt, pos);
            throw new InvalidInputDataException(
                "Unable to decode block node type id from unknown identifier int " + idInt + " @ " + pos);
        }

        BlockNodeType type = universe.getUniverse().getNodeType(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown block node type id {} @ {} in universe {}", typeId, pos,
                universe.getId());
            throw new InvalidInputDataException(
                "Unable to decode unknown block node type id " + typeId + " @ " + pos + " in universe " +
                    universe.getId());
        }

        if (!universe.hasNodeSyncing(type)) {
            GLLog.error("Tried to decode block node {} @ {} in universe {} but it has no packet decoder.", type.getId(),
                pos, universe.getId());
            throw new InvalidInputDataException(
                "Tried to decode block node " + type.getId() + " @ " + pos + " in universe " + universe.getId() +
                    " but it has no packet decoder.");
        }
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);

        BlockNode node = syncing.decode(buf, ctx);

        return new NodePos(pos, node);
    }

    /**
     * Writes this link pos to a packet.
     *
     * @param linkPos  the link-pos to be encoded.
     * @param buf      the buffer to write to.
     * @param ctx      the message context.
     * @param universe the universe that the link-pos's link key is associated with.
     */
    public static void encodeLinkPos(@NotNull LinkPos linkPos, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx,
                                     @NotNull LNSSyncedUniverse universe) {
        encodeNodePos(linkPos.first(), buf, ctx, universe);
        encodeNodePos(linkPos.second(), buf, ctx, universe);
        buf.writeVarUnsignedInt(LNSNetworking.ID_CACHE.getId(ctx.getConnection(), linkPos.key().getType().getId()));
        universe.getLinkKeySyncing(linkPos.key().getType()).encode(linkPos.key(), buf, ctx);
    }

    /**
     * Decodes a link pos from a packet.
     *
     * @param buf      the buffer to read from.
     * @param ctx      the message context.
     * @param universe the universe containing the decoders that this will use.
     * @return a newly decoded link pos, or <code>null</code> if decoding failed.
     * @throws InvalidInputDataException if there was an error while decoding the link pos.
     */
    public static @NotNull LinkPos decodeLinkPos(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx ctx,
                                                 @NotNull LNSSyncedUniverse universe) throws InvalidInputDataException {
        NodePos first = decodeNodePos(buf, ctx, universe);

        NodePos second = decodeNodePos(buf, ctx, universe);

        int idInt = buf.readVarUnsignedInt();
        Identifier typeId = LNSNetworking.ID_CACHE.getObj(ctx.getConnection(), idInt);
        if (typeId == null) {
            GLLog.warn("Unable to decode link key type id from unknown identifier int {} @ {}-{}", idInt, first,
                second);
            throw new InvalidInputDataException(
                "Unable to decode link key type id from unknown identifier int " + idInt + " @ " + first + "-" +
                    second);
        }

        LinkKeyType type = universe.getUniverse().getLinkKeyType(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown link key type id {} @ {}-{} in universe {}", typeId, first, second,
                universe.getId());
            throw new InvalidInputDataException(
                "Unable to decode unknown link key type id " + typeId + " @ " + first + "-" + second + " in universe " +
                    universe.getId());
        }

        if (!universe.hasLinkKeySyncing(type)) {
            GLLog.error("Tried to decode link key {} @ {}-{} in universe {} but it has no packet decoder.", typeId,
                first, second, universe.getId());
            throw new InvalidInputDataException(
                "Tried to decode link key " + typeId + " @ " + first + "-" + second + " in universe " +
                    universe.getId() + " but it has no packet decoder.");
        }
        LinkKeySyncing decoder = universe.getLinkKeySyncing(type);

        LinkKey key = decoder.decode(buf, ctx);

        return new LinkPos(first, second, key);
    }
}
