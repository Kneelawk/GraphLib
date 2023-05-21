package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Used for decoding the old format of block node where both node key and block node were represented by the same NBT
 * tag.
 * <p>
 * This is only needed for decoding block nodes and node keys from GraphLib versions prior to
 * <code>v1.0.0-alpha.16</code>. Decoding nodes from version <code>v1.0.0-alpha.16</code> and later do not need one
 * of these registered.
 */
public interface LegacyBlockNodeDecoder {
    /**
     * Decodes a block node from the shared NBT tag.
     *
     * @param tag the NBT tag to decode from.
     * @param ctx the block node context.
     * @return a newly decoded block node, or <code>null</code> if none could be decoded.
     */
    @Nullable BlockNode decodeBlockNode(@Nullable NbtElement tag, @NotNull BlockNodeContext ctx);

    /**
     * Decodes a node key from the shared NBT tag.
     *
     * @param tag the NBT tag to decode from.
     * @return a newly decoded node key, or <code>null</code> if none could be decoded.
     */
    @Nullable NodeKey decodeNodeKey(@Nullable NbtElement tag);
}
