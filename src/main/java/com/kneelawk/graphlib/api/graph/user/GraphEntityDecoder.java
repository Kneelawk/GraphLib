package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Decodes a graph entity from an NBT tag.
 *
 * @param <G> the type of graph entity to decode.
 */
@FunctionalInterface
public interface GraphEntityDecoder<G extends GraphEntity<G>> {
    /**
     * Decodes a graph entity from an NBT tag.
     *
     * @param tag the NBT tag to decode from.
     * @param ctx the graph entity context for the new graph entity.
     * @return a newly decoded graph entity, or <code>null</code> if no graph entity could be decoded.
     */
    @Nullable G decode(@Nullable NbtElement tag, @NotNull GraphEntityContext ctx);
}
