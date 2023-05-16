package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Describes the unique-data associated with a {@link BlockNode} that is used as its key.
 */
public interface UniqueData {
    /**
     * Gets the type id associated with this unique-data.
     * <p>
     * This should be the same as is registered with {@link GraphUniverse#addDecoder(Identifier, BlockNodeDecoder)}.
     *
     * @return this unique-data's type id.
     * @see BlockNode#getTypeId()
     */
    @NotNull Identifier getTypeId();

    /**
     * Encodes this unique data to an NBT element.
     * <p>
     * This can return <code>null</code> if the unique-data's type is all that needs to be stored.
     *
     * @return this as an NBT element.
     */
    @Nullable NbtElement toTag();
}
