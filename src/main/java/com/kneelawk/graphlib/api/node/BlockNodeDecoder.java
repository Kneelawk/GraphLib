package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

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

    /**
     * Decodes a {@link UniqueData} from an NBT element.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link UniqueData#toTag()}.
     * <p>
     * This method is not used in block node decoding, but is instead used where unique data needs to be cached apart
     * from the data's associated block node.
     *
     * @param tag the NBT element used to decode the unique data.
     * @return the decoded unique data.
     */
    @Nullable UniqueData createUniqueDataFromTag(@Nullable NbtElement tag);
}
