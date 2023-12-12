package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Used for decoding a {@link BlockNode} from an NBT element.
 */
@FunctionalInterface
public interface BlockNodeDecoder<N extends BlockNode> {
    /**
     * Decodes a {@link BlockNode} from an NBT element.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link BlockNode#toTag()}.
     *
     * @param tag the NBT element used to decode the block node.
     * @return the decoded block node.
     */
    @Nullable N decode(@Nullable NbtElement tag);
}
