package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.LinkEntityContext;

/**
 * Decoder for {@link LinkEntity}s.
 */
public interface LinkEntityDecoder {
    /**
     * Decodes a {@link LinkEntity} from the given tag.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link LinkEntity#toTag()}.
     *
     * @param tag the NBT element to decode from.
     * @param ctx the link entity context for the new entity.
     * @return a newly decode link entity, or <code>null</code> if a link entity could not be decoded.
     */
    @Nullable LinkEntity decode(NbtElement tag, LinkEntityContext ctx);
}
