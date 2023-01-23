package com.kneelawk.graphlib.node;

import com.kneelawk.graphlib.node.BlockNode;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

/**
 * Used for decoding a {@link BlockNode} from an NBT element.
 */
public interface BlockNodeDecoder {
    /**
     * Decodes a {@link BlockNode} from an NBT element.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link BlockNode#toTag()}.
     *
     * @param tag the NBT element used to decode the block node.
     * @return the decoded block node.
     */
    @Nullable BlockNode createBlockNodeFromTag(@Nullable NbtElement tag);
}
