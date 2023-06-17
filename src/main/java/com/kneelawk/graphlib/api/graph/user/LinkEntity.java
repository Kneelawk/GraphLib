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
    ParentNetIdSingle<LinkEntity> NET_PARENT = GLNet.LINK_ENTITY_PARENT;

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
     * Called when this link entity's graph is about to be unloaded.
     */
    default void onUnload() {}

    /**
     * Called when this link entity's block link has been deleted.
     */
    default void onDelete() {}
}
