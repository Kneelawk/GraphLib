package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.ParentNetIdSingle;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.impl.net.GLNet;

/**
 * Mutable data associated with a link, similar to a BlockEntity.
 */
public interface LinkEntity {
    /**
     * LibNetworkStack net parent for link entities.
     */
    @NotNull ParentNetIdSingle<LinkEntity> NET_PARENT = GLNet.LINK_ENTITY_PARENT;

    /**
     * Called when this link entity is initialized in a graph, to give this its context.
     *
     * @param ctx this link entity's context.
     */
    void onInit(@NotNull LinkEntityContext ctx);

    /**
     * Gets the link entity context this was created with.
     *
     * @return this link entity's context.
     */
    @NotNull LinkEntityContext getContext();

    /**
     * Get this link entity's type id.
     * <p>
     * The id returned here must be the same as the one registered with
     * {@link GraphUniverse#addLinkEntityType(LinkEntityType)}.
     *
     * @return this link entity's type id.
     */
    @NotNull LinkEntityType getType();

    /**
     * Encodes this link entity as an NBT tag.
     *
     * @return this link entity as an NBT tag.
     */
    @Nullable NbtElement toTag();

    /**
     * Encodes this link entity as a packet for server to client synchronization.
     *
     * @param buf the buffer to write to.
     * @param ctx the message context.
     */
    default void toPacket(@NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {}

    /**
     * Called after this link entity has been initialized if it was just newly added instead of just being loaded.
     * <p>
     * Client-side, this means that the new entity addition was observed by the client.
     */
    default void onAdded() {}

    /**
     * Called after this link entity has been initialized if it was just loaded instead of being newly added.
     * <p>
     * Client-side, this means that the entity was simply received as part of a bulk chunk read.
     */
    default void onLoaded() {}

    /**
     * Called when this link entity's graph is about to be unloaded.
     */
    default void onUnload() {}

    /**
     * Called when this link entity's block link has been deleted.
     */
    default void onDelete() {}

    /**
     * Called when this entity has been created, but it is discovered that another instance of this entity has already
     * been created previously and that this instance should be discarded.
     */
    default void onDiscard() {}
}
