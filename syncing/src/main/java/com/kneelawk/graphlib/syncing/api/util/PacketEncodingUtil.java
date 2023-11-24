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

package com.kneelawk.graphlib.syncing.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.impl.GLNet;

/**
 * Contains various utility methods for en/decoding various types to/from packets.
 */
public class PacketEncodingUtil {
    private PacketEncodingUtil() {}

    /**
     * Writes this NodePos to a packet.
     *
     * @param nodePos  the node-pos to be encoded.
     * @param buf      the buffer to write to.
     * @param ctx      the packet's message context.
     * @param universe the universe that the node-pos's node is associated with.
     */
    public static void encodeNodePos(@NotNull NodePos nodePos, @NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx,
                                     @NotNull SyncedUniverse universe) {
        buf.writeBlockPos(nodePos.pos());
        buf.writeVarUnsignedInt(GLNet.ID_CACHE.getId(ctx.getConnection(), nodePos.node().getType().getId()));
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
                                                 @NotNull SyncedUniverse universe) throws InvalidInputDataException {
        BlockPos pos = buf.readBlockPos();

        int idInt = buf.readVarUnsignedInt();
        Identifier typeId = GLNet.ID_CACHE.getObj(ctx.getConnection(), idInt);
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
}
