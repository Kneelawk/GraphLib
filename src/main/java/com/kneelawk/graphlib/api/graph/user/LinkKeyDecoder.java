package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Used for decoding link keys.
 */
public interface LinkKeyDecoder {
    /**
     * Decodes a link key from an NBT tag.
     * <p>
     * Note: the supplied NBT tag should be exactly the same as the one returned by {@link LinkKey#toTag()}.
     *
     * @param tag the NBT tag to decode from.
     * @return a newly decoded link key, or <code>null</code> if a link key could not be decoded.
     */
    @Nullable LinkKey decode(@Nullable NbtElement tag);
}
