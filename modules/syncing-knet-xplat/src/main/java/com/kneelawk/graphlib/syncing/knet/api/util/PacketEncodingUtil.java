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

package com.kneelawk.graphlib.syncing.knet.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkKeySyncing;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.Palette;

/**
 * Contains various utility methods for en/decoding various types to/from packets.
 */
public final class PacketEncodingUtil {
    private PacketEncodingUtil() {}

    /**
     * Syncing for {@link EmptyLinkKey}.
     */
    public static final LinkKeySyncing EMPTY_KEY_SYNCING = LinkKeySyncing.ofNoOp(() -> EmptyLinkKey.INSTANCE);

    /**
     * A payload representing everything for a {@link NodePos} that would not go in the header.
     *
     * @param pos     the block position.
     * @param typeId  the node's type id.
     * @param nodeBuf the buffer holding the node's encoded data.
     */
    public record NodePosPayload(@NotNull BlockPos pos, @NotNull Identifier typeId, @NotNull NetByteBuf nodeBuf) {
        /**
         * This payload's codec.
         */
        public static final PayloadCodec<NodePosPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
            buf.writeBlockPos(payload.pos);
            buf.writeIdentifier(payload.typeId);
            buf.writeInt(payload.nodeBuf.readableBytes());
            buf.writeBytes(payload.nodeBuf, payload.nodeBuf.readerIndex(), payload.nodeBuf.readableBytes());
        }, buf -> {
            BlockPos pos = buf.readBlockPos();
            Identifier typeId = buf.readIdentifier();

            int nodeBufLen = buf.readInt();
            NetByteBuf nodeBuf = NetByteBuf.buffer(nodeBufLen);
            buf.readBytes(nodeBuf, nodeBufLen);

            return new NodePosPayload(pos, typeId, nodeBuf);
        });
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
        BlockNodeType type = universe.getUniverse().getNodeType(payload.typeId);
        if (type == null)
            throw new PayloadHandlingErrorException("Invalid block node type: " + payload.typeId + " @ " + payload.pos);
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);

        BlockNode node = syncing.decode(payload.nodeBuf);

        return new NodePos(payload.pos, node);
    }

    /**
     * A smaller payload representing a {@link NodePos}, while allowing node data and palette data to go in a header.
     *
     * @param pos    the block position of the {@link NodePos}.
     * @param typeId the integer for the node's type id to be looked up in the palette.
     */
    public record NodePosSmallPayload(@NotNull BlockPos pos, int typeId) {
        /**
         * This payload's codec.
         */
        public static final PayloadCodec<NodePosSmallPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
            buf.writeBlockPos(payload.pos);
            buf.writeVarInt(payload.typeId);
        }, buf -> {
            BlockPos pos = buf.readBlockPos();
            int typeId = buf.readVarInt();

            return new NodePosSmallPayload(pos, typeId);
        });
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
    public static @NotNull NodePosSmallPayload encodeNodePosSmall(@NotNull NodePos nodePos,
                                                                  @NotNull NetByteBuf nodeBuf,
                                                                  @NotNull Palette<Identifier> palette,
                                                                  @NotNull KNetSyncedUniverse universe) {
        BlockNodeType type = nodePos.node().getType();
        Identifier typeId = type.getId();

        int typeInt = palette.keyFor(typeId);

        universe.getNodeSyncing(type).encode(nodePos.node(), nodeBuf);

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
    public static @NotNull NodePos decodeNodePosSmall(@NotNull NodePosSmallPayload payload,
                                                      @NotNull NetByteBuf nodeBuf,
                                                      @NotNull Palette<Identifier> palette,
                                                      @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        Identifier typeId = palette.get(payload.typeId);
        if (typeId == null) throw new PayloadHandlingErrorException(
            "Invalid block node type int: " + payload.typeId + " @ " + payload.pos);
        BlockNodeType type = universe.getUniverse().getNodeType(typeId);
        if (type == null)
            throw new PayloadHandlingErrorException("Invalid block node type: " + typeId + " @ " + payload.pos);
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);

        BlockNode node = syncing.decode(nodeBuf);

        return new NodePos(payload.pos, node);
    }
}
