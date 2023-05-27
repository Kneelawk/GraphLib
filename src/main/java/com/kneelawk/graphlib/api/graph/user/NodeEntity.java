package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Mutable data associated with a block node, similar to a BlockEntity.
 */
public interface NodeEntity {
    /**
     * Get this node entity's type id.
     * <p>
     * The id returned here must be the same as the one registered with
     * {@link GraphUniverse#addNodeEntityDecoder(Identifier, NodeEntityDecoder)}.
     *
     * @return this node entity's type id.
     */
    @NotNull Identifier getTypeId();

    /**
     * Encodes this node entity as an NBT tag.
     *
     * @return this node entity as an NBT tag.
     */
    @Nullable NbtElement toTag();

    /**
     * Called when this node entity's graph is about to be unloaded.
     */
    void onUnload();

    /**
     * Called when this node entity's block node has been deleted.
     */
    void onDelete();
}