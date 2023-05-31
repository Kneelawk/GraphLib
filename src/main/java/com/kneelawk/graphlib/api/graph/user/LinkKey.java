package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.LinkContext;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;

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

    /**
     * Checks whether this specific link should have a link entity associated with it.
     *
     * @param ctx the link context for this link.
     * @return <code>true</code> if this link should have a link entity associated with it.
     */
    default boolean shouldHaveLinkEntity(@NotNull LinkContext ctx) {
        return false;
    }

    /**
     * Creates a new link entity that will be associated with this link.
     *
     * @param ctx the link entity context for the new link entity.
     * @return a newly created link entity, or <code>null</code> if a link entity could not be created.
     */
    default @Nullable LinkEntity createLinkEntity(@NotNull LinkEntityContext ctx) {
        return null;
    }

    /**
     * Link keys are compared based on their hash-code and equals functions.
     * <p>
     * Link keys must always implement consistent hash-code and equals functions, as this allows the graph world to be
     * able to correctly evaluate if links need to be removed or added at a given position.
     *
     * @return the hash-code of this link key's data.
     */
    @Override
    int hashCode();

    /**
     * Link keys are compared based on their hash-code and equals functions.
     * <p>
     * Link keys must always implement consistent hash-code and equals functions, as this allows the graph world to be
     * able to correctly evaluate if links need to be removed or added at a given position.
     *
     * @param o the other link key to compare this link key to.
     * @return <code>true</code> if these two link keys hold the same data, <code>false</code> otherwise.
     */
    @Override
    boolean equals(@Nullable Object o);
}
