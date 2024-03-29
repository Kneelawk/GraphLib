package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.ParentNetIdSingle;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.impl.net.GLNet;

/**
 * Mutable data associated with a block node, similar to a BlockEntity.
 */
public interface NodeEntity {
    /**
     * LibNetworkStack net parent for node entities.
     */
    @NotNull ParentNetIdSingle<NodeEntity> NET_PARENT = GLNet.NODE_ENTITY_PARENT;

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
    @NotNull NodeEntityContext getContext();

    /**
     * Get this node entity's type id.
     * <p>
     * The id returned here must be the same as the one registered with
     * {@link GraphUniverse#addNodeEntityType(NodeEntityType)}.
     *
     * @return this node entity's type id.
     */
    @NotNull NodeEntityType getType();

    /**
     * Encodes this node entity as an NBT tag.
     *
     * @return this node entity as an NBT tag.
     */
    @Nullable NbtElement toTag();

    /**
     * Encodes this node entity to a packet for server to client synchronization.
     *
     * @param buf the buffer to write to.
     * @param ctx the message context.
     */
    default void toPacket(@NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {}

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
