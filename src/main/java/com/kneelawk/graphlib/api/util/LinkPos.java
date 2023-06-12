package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;

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
    public LinkPos(BlockPos firstPos, BlockNode firstNode, BlockPos secondPos, BlockNode secondNode, LinkKey key) {
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
        LinkKeyDecoder decoder = universe.getLinkKeyType(typeId);
        if (decoder == null) return null;
        LinkKey key = decoder.decode(nbt.get("key"));
        if (key == null) return null;

        return new LinkPos(first, second, key);
    }
}
