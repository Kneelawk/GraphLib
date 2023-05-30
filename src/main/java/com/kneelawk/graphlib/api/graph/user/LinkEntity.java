package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Mutable data associated with a link, similar to a BlockEntity.
 */
public interface LinkEntity {
    /**
     * Get this link entity's type id.
     * <p>
     * The id returned here must be the same as the one registered with
     * {@link GraphUniverse#addLinkEntityDecoder(Identifier, LinkEntityDecoder)}.
     *
     * @return this link entity's type id.
     */
    @NotNull Identifier getTypeId();

    /**
     * Encodes this link entity as an NBT tag.
     *
     * @return this link entity as an NBT tag.
     */
    @Nullable NbtElement toTag();

    /**
     * Called when this link entity's graph is about to be unloaded.
     */
    void onUnload();

    /**
     * Called when this link entity's block link has been deleted.
     */
    void onDelete();
}
