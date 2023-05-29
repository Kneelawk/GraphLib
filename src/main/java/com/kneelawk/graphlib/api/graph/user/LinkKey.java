package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

/**
 * The data stored in a link between nodes.
 */
public interface LinkKey {
    /**
     * Gets the type id of this link key.
     * <p>
     * Note: this is the same type id as is used in registering link key decoders,
     * {@link com.kneelawk.graphlib.api.graph.GraphUniverse#addLinkKeyDecoder(Identifier, LinkKeyDecoder)}.
     *
     * @return this link key's type id.
     */
    @NotNull Identifier getTypeId();

    /**
     * Encodes this link key as an NBT tag.
     *
     * @return this link key as an NBT tag.
     */
    @Nullable NbtElement toTag();
}
