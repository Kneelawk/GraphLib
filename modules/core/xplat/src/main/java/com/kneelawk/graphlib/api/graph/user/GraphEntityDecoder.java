package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Decodes a graph entity from an NBT tag.
 */
@FunctionalInterface
public interface GraphEntityDecoder {
    /**
     * Decodes a graph entity from an NBT tag.
     *
     * @param tag the NBT tag to decode from.
     * @return a newly decoded graph entity, or <code>null</code> if no graph entity could be decoded.
     */
    @Nullable GraphEntity<?> decode(@Nullable NbtElement tag);
}
