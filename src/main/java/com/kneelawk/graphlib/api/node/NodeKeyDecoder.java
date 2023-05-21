package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * A decoder for node-keys.
 */
@FunctionalInterface
public interface NodeKeyDecoder {
    /**
     * Decodes a {@link NodeKey} from an NBT element.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link NodeKey#toTag()}.
     *
     * @param tag the NBT element used to decode the unique data.
     * @return the decoded unique data.
     */
    @Nullable NodeKey decode(@Nullable NbtElement tag);
}
