package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;

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
     * Gets a link pos codec for link poses in the given universe.
     *
     * @param universe the universe to find link poses in.
     * @return a link pos codec for link poses in the given universe.
     */
    public static Codec<LinkPos> codec(GraphUniverse universe) {
        return RecordCodecBuilder.create(instance -> instance.group(
            NodePos.codec(universe).fieldOf("first").forGetter(LinkPos::first),
            NodePos.codec(universe).fieldOf("second").forGetter(LinkPos::second),
            LinkKey.mapCodec(universe).forGetter(LinkPos::key)
        ).apply(instance, LinkPos::new));
    }

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
    public void toNbt(@NotNull CompoundTag nbt) {
        nbt.put("first", first.toNbt());
        nbt.put("second", second.toNbt());
        nbt.putString("keyType", key.getType().getId().toString());
        Tag keyNbt = key.toTag();
        if (keyNbt != null) {
            nbt.put("key", keyNbt);
        }
    }

    /**
     * Encodes this link pos to an NBT compound.
     *
     * @return the NBT compound containing this link pos's encoded data.
     */
    public @NotNull CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
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
    public static @Nullable LinkPos fromNbt(@NotNull CompoundTag nbt, @NotNull GraphUniverse universe) {
        NodePos first = NodePos.fromNbt(nbt.getCompound("first"), universe);
        if (first == null) return null;
        NodePos second = NodePos.fromNbt(nbt.getCompound("second"), universe);
        if (second == null) return null;

        ResourceLocation typeId = new ResourceLocation(nbt.getString("keyType"));
        LinkKeyType type = universe.getLinkKeyType(typeId);
        if (type == null) return null;
        LinkKey key = type.getCodec().decode(nbt.get("key"));
        if (key == null) return null;

        return new LinkPos(first, second, key);
    }
}
