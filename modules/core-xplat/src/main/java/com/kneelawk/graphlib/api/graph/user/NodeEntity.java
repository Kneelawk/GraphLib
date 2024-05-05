package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.util.codec.CustomKeyDispatchCodec;

/**
 * Mutable data associated with a block node, similar to a BlockEntity.
 */
public interface NodeEntity {
    /**
     * Gets the node entity map codec for node entities in the given universe.
     *
     * @param universe the universe to find node entities in.
     * @return a node entity map codec for node entities in the given universe.
     */
    static MapCodec<NodeEntity> mapCodec(GraphUniverse universe) {
        return new CustomKeyDispatchCodec<>("entityType", "entity", ResourceLocation.CODEC,
            entity -> DataResult.success(entity.getType().getId()), typeId -> {
            NodeEntityType type = universe.getNodeEntityType(typeId);
            if (type == null) {
                return DataResult.error(
                    () -> "No node entity exists with type '" + typeId + "' in universe '" + universe.getId() + "'");
            }
            return DataResult.success(type.getCodec());
        });
    }

    /**
     * Called when this node entity is initialized in a graph, to give this its context.
     *
     * @param ctx this node entity's context.
     */
    void onInit(@NotNull NodeEntityContext ctx);

    /**
     * Gets the node entity context this was created with.
     *
     * @return this node entity's context.
     */
    @NotNull
    NodeEntityContext getContext();

    /**
     * Get this node entity's type id.
     * <p>
     * The id returned here must be the same as the one registered with
     * {@link GraphUniverse#addNodeEntityType(NodeEntityType)}.
     *
     * @return this node entity's type id.
     */
    @NotNull
    NodeEntityType getType();

    /**
     * Called after this node entity has been initialized if it was just newly added instead of just being loaded.
     * <p>
     * Client-side, this means that the new entity addition was observed by the client.
     */
    default void onAdded() {}

    /**
     * Called after this node entity has been initialized if it was just loaded instead of being newly added.
     * <p>
     * Client-side, this means that the entity was simply received as part of a bulk chunk read.
     */
    default void onLoaded() {}

    /**
     * Called when this node entity's graph is about to be unloaded.
     */
    default void onUnload() {}

    /**
     * Called when this node entity's block node has been deleted.
     */
    default void onDelete() {}

    /**
     * Called when this entity has been created, but it is discovered that another instance of this entity has already
     * been created previously and that this instance should be discarded.
     */
    default void onDiscard() {}
}
