package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Mutable data associated with a link, similar to a BlockEntity.
 */
public interface LinkEntity {
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
    void onUnload();

    /**
     * Called when this link entity's block link has been deleted.
     */
    void onDelete();
}
