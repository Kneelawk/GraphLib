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

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

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
import com.kneelawk.knet.api.handling.PayloadHandlingException;

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
     * Payload representing a {@link NodePos}.
     *
     * @param blockPos    the block position.
     * @param nodePayload the block node's payload.
     * @param type        the type of the block node.
     */
    public record NodePosPayload(@NotNull BlockPos blockPos, @NotNull Object nodePayload, @NotNull BlockNodeType type) {
        /**
         * Gets the codec for this payload.
         *
         * @param universe the universe this payload is associated with.
         * @return the coded for this payload.
         */
        public static PayloadCodec<NodePosPayload> codec(KNetSyncedUniverse universe) {
            return new PayloadCodec<>((buf, payload) -> {
                buf.writeBlockPos(payload.blockPos);
                buf.writeIdentifier(payload.type.getId());
                universe.getNodeSyncing(payload.type).encodePayload(payload.nodePayload, buf);
            }, buf -> {
                BlockPos blockPos = buf.readBlockPos();

                Identifier typeId = buf.readIdentifier();
                BlockNodeType type = universe.getUniverse().getNodeType(typeId);
                if (type == null) throw new DecoderException("Invalid block node type: " + typeId + " @ " + blockPos);
                BlockNodeSyncing syncing = universe.getNodeSyncing(type);

                Object payload = syncing.decodePayload(buf);

                return new NodePosPayload(blockPos, payload, type);
            });
        }
    }

    /**
     * Encodes a {@link NodePos} into a {@link NodePosPayload}.
     *
     * @param nodePos  the {@link NodePos} to encode.
     * @param universe the universe that the {@link NodePos} exists in.
     * @return a payload representing the {@link NodePos}.
     */
    public static @NotNull NodePosPayload encodeNodePos(@NotNull NodePos nodePos,
                                                        @NotNull KNetSyncedUniverse universe) {
        BlockNodeType type = nodePos.node().getType();
        BlockNodeSyncing syncing = universe.getNodeSyncing(type);
        Object nodePayload = syncing.encode(nodePos.node());

        return new NodePosPayload(nodePos.pos(), nodePayload, type);
    }

    /**
     * Decodes a {@link NodePos} from a payload.
     *
     * @param payload  the payload to decode from.
     * @param universe the universe that the {@link NodePos} exists in.
     * @return the decoded {@link NodePos}.
     * @throws PayloadHandlingException if an error occurs while decoding.
     */
    public static @NotNull NodePos decodeNodePos(@NotNull NodePosPayload payload, @NotNull KNetSyncedUniverse universe)
        throws PayloadHandlingException {
        BlockNodeSyncing syncing = universe.getNodeSyncing(payload.type);
        BlockNode node = syncing.decode(payload.nodePayload);

        return new NodePos(payload.blockPos, node);
    }

    /**
     * Payload representing a {@link NodePos}, using an integer for the type id.
     *
     * @param blockPos    the block position.
     * @param nodePayload the block node's payload.
     * @param typeInt     the type of the block node.
     */
    public record NodePosIntPayload(@NotNull BlockPos blockPos, @NotNull Object nodePayload, int typeInt) {
        /**
         * Gets the codec for this payload.
         *
         * @param universe the universe this payload is associated with.
         * @param idLookup the map from integers to {@link Identifier}s.
         * @return the codec for this payload.
         */
        public static PayloadCodec<NodePosIntPayload> codec(KNetSyncedUniverse universe,
                                                            Int2ObjectMap<Identifier> idLookup) {
            return new PayloadCodec<>((buf, payload) -> {
                buf.writeBlockPos(payload.blockPos);
                buf.writeVarInt(payload.typeInt);

                Identifier typeId = idLookup.get(payload.typeInt);
                if (typeId == null) throw new EncoderException("Invalid block node type int: " + payload.typeInt);
                BlockNodeType type = universe.getUniverse().getNodeType(typeId);
                if (type == null) throw new EncoderException("Invalid block node type: " + typeId);
                BlockNodeSyncing syncing = universe.getNodeSyncing(type);

                syncing.encodePayload(payload.nodePayload, buf);
            }, buf -> {
                BlockPos blockPos = buf.readBlockPos();

                int typeInt = buf.readVarInt();
                Identifier typeId = idLookup.get(typeInt);
                if (typeId == null)
                    throw new DecoderException("Received invalid block node type int: " + typeInt + " @ " + blockPos);
                BlockNodeType type = universe.getUniverse().getNodeType(typeId);
                if (type == null) throw new DecoderException("Invalid block node type: " + typeId + " @ " + blockPos);
                BlockNodeSyncing syncing = universe.getNodeSyncing(type);

                Object payload = syncing.decodePayload(buf);

                return new NodePosIntPayload(blockPos, payload, typeInt);
            });
        }
    }
}
