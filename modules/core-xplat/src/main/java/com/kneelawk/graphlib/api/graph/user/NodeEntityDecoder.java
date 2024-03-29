package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Decoder for {@link NodeEntity}s.
 */
@FunctionalInterface
public interface NodeEntityDecoder {
    /**
     * Decodes a {@link NodeEntity} from the given tag.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link NodeEntity#toTag()}.
     *
     * @param tag the NBT element used to decode this node entity.
     * @return a newly decoded node entity.
     */
    @Nullable NodeEntity decode(@Nullable NbtElement tag);
}
