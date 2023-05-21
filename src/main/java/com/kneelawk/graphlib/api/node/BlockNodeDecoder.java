package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Used for decoding a {@link BlockNode} from an NBT element.
 */
@FunctionalInterface
public interface BlockNodeDecoder {
    /**
     * Decodes a {@link BlockNode} from an NBT element.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link BlockNode#toTag()}.
     *
     * @param tag the NBT element used to decode the block node.
     * @param ctx the context this node is to be created with.
     * @return the decoded block node.
     */
    @Nullable BlockNode decode(@Nullable NbtElement tag, @NotNull BlockNodeContext ctx);
}
