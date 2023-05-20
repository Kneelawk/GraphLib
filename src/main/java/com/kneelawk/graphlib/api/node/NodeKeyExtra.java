package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Describes the unique-data associated with a {@link BlockNode} that is used as its key.
 */
public interface NodeKeyExtra {
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

    /**
     * All unique-data must implement consistent {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
     *
     * @return this unique-data's hash-code.
     */
    int hashCode();

    /**
     * All unique-data must implement consistent {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
     *
     * @param other the other object this is being compared to.
     * @return whether this object and the other object are equals.
     */
    boolean equals(Object other);
}
