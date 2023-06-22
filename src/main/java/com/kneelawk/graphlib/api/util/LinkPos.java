package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.net.GLNet;

/**
 * Represents a positioned unique link in a way that can be looked up.
 * <p>
 * Note: this type is non-directional. A link from 'A' to 'B' is the same as a link from 'B' to 'A'. The
 * {@link #equals(Object)} and {@link #hashCode()} methods reflect this.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param key    the key of this link that makes it unique among all the links between the same two nodes.
 */
public record LinkPos(@NotNull NodePos first, @NotNull NodePos second, @NotNull LinkKey key) {
    /**
     * Creates a new link pos from raw positions, nodes, and the link key.
     *
     * @param firstPos   the block position of the first end of this link.
     * @param firstNode  the block node at the first end of this link.
     * @param secondPos  the block position of the second end of this link.
     * @param secondNode the block node at the second end of this link.
     * @param key        the key of this link.
     */
    public LinkPos(@NotNull BlockPos firstPos, @NotNull BlockNode firstNode, @NotNull BlockPos secondPos,
                   @NotNull BlockNode secondNode, @NotNull LinkKey key) {
        this(new NodePos(firstPos, firstNode), new NodePos(secondPos, secondNode), key);
    }

    /**
     * Gets the node pos at the opposite end of this link from the given pos.
     *
     * @param pos the pos to get the opposite end of the link from.
     * @return the pos at the opposite end of this link from the given pos.
     */
    public NodePos other(@NotNull NodePos pos) {
        if (first.equals(pos)) {
            return second;
        } else {
            return first;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkPos linkKey = (LinkPos) o;

        if (!key.equals(linkKey.key)) return false;

        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = result ^ second.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    /**
     * Encodes this link pos to the given NBT compound.
     * <p>
     * Note: this writes to the {@code first}, {@code second}, {@code keyType}, and {@code key} elements.
     *
     * @param nbt the NBT compound to write to.
     */
    public void toNbt(@NotNull NbtCompound nbt) {
        nbt.put("first", first.toNbt());
        nbt.put("second", second.toNbt());
        nbt.putString("keyType", key.getType().getId().toString());
        NbtElement keyNbt = key.toTag();
        if (keyNbt != null) {
            nbt.put("key", keyNbt);
        }
    }

    /**
     * Encodes this link pos to an NBT compound.
     *
     * @return the NBT compound containing this link pos's encoded data.
     */
    public @NotNull NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        toNbt(nbt);
        return nbt;
    }

    /**
     * Writes this link pos to a packet.
     *
     * @param buf the buffer to write to.
     * @param ctx the message context.
     */
    public void toPacket(@NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {
        first.toPacket(buf, ctx);
        second.toPacket(buf, ctx);
        buf.writeVarUnsignedInt(GLNet.ID_CACHE.getId(ctx.getConnection(), key.getType().getId()));
        key.toPacket(buf, ctx);
    }

    /**
     * Decodes a link pos from an NBT compound.
     *
     * @param nbt      the NBT compound to decode from.
     * @param universe the universe containing the decoders that this will use.
     * @return a newly decoded link pos.
     */
    public static @Nullable LinkPos fromNbt(@NotNull NbtCompound nbt, @NotNull GraphUniverse universe) {
        NodePos first = NodePos.fromNbt(nbt.getCompound("first"), universe);
        if (first == null) return null;
        NodePos second = NodePos.fromNbt(nbt.getCompound("second"), universe);
        if (second == null) return null;

        Identifier typeId = new Identifier(nbt.getString("keyType"));
        LinkKeyType type = universe.getLinkKeyType(typeId);
        if (type == null) return null;
        LinkKey key = type.getDecoder().decode(nbt.get("key"));
        if (key == null) return null;

        return new LinkPos(first, second, key);
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
    public static @NotNull LinkPos fromPacket(@NotNull NetByteBuf buf, @NotNull IMsgReadCtx ctx,
                                              @NotNull GraphUniverse universe) throws InvalidInputDataException {
        NodePos first = NodePos.fromPacket(buf, ctx, universe);

        NodePos second = NodePos.fromPacket(buf, ctx, universe);

        int idInt = buf.readVarUnsignedInt();
        Identifier typeId = GLNet.ID_CACHE.getObj(ctx.getConnection(), idInt);
        if (typeId == null) {
            GLLog.warn("Unable to decode link key type id from unknown identifier int {} @ {}-{}", idInt, first,
                second);
            throw new InvalidInputDataException(
                "Unable to decode link key type id from unknown identifier int " + idInt + " @ " + first + "-" +
                    second);
        }

        LinkKeyType type = universe.getLinkKeyType(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown link key type id {} @ {}-{} in universe {}", typeId, first, second,
                universe.getId());
            throw new InvalidInputDataException(
                "Unable to decode unknown link key type id " + typeId + " @ " + first + "-" + second + " in universe " +
                    universe.getId());
        }

        LinkKeyPacketDecoder decoder = type.getPacketDecoder();
        if (decoder == null) {
            GLLog.error("Tried to decode link key {} @ {}-{} in universe {} but it has no packet decoder.", typeId,
                first, second, universe.getId());
            throw new InvalidInputDataException(
                "Tried to decode link key " + typeId + " @ " + first + "-" + second + " in universe " +
                    universe.getId() + " but it has no packet decoder.");
        }

        LinkKey key = decoder.decode(buf, ctx);

        return new LinkPos(first, second, key);
    }
}
