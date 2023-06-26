package com.kneelawk.graphlib.api.graph.user;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.ParentNetIdSingle;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.impl.net.GLNet;

/**
 * Arbitrary data that can be stored in a graph.
 *
 * @param <G> this graph entity class.
 */
public interface GraphEntity<G extends GraphEntity<G>> {
    /**
     * LibNetworkStack net parent for graph entities.
     */
    @SuppressWarnings("rawtypes")
    @NotNull ParentNetIdSingle<GraphEntity> NET_PARENT = GLNet.GRAPH_ENTITY_PARENT;

    /**
     * Called when the graph entity is initialized in a graph, to give this its context.
     *
     * @param ctx this graph entity's context.
     */
    void onInit(@NotNull GraphEntityContext ctx);

    /**
     * Gets the graph entity context this was created with.
     *
     * @return this graph entity's context.
     */
    @NotNull GraphEntityContext getContext();

    /**
     * Gets this graph entity's type.
     *
     * @return this graph entity's type.
     */
    @NotNull GraphEntityType<?> getType();

    /**
     * Encodes this graph entity as an NBT tag.
     *
     * @return this graph entity as an NBT tag.
     */
    @Nullable NbtElement toTag();

    /**
     * Encodes this graph entity as a packet for server to client synchronization.
     *
     * @param buf the buffer to write to.
     * @param ctx the message context.
     */
    default void toPacket(@NotNull NetByteBuf buf, @NotNull IMsgWriteCtx ctx) {}

    /**
     * Called right before this entity's associated graph is deleted.
     */
    default void onDestroy() {}

    /**
     * Called right before this entity's associated graph is unloaded.
     * <p>
     * Note: This cannot cancel graph unloading, however, any changes made here will be saved.
     */
    default void onUnload() {}

    /**
     * Called when this entity has been created, but it is discovered that another instance of this entity has already
     * been created previously and that this instance should be discarded.
     */
    default void onDiscard() {}

    /**
     * Called when a new node is created in this graph.
     * <p>
     * Note: often adding a new node to this graph will first involve creating a new graph for that node and then
     * merging that graph into this one.
     *
     * @param node       the new node added to the graph.
     * @param nodeEntity the node's entity, if any.
     */
    default void onNodeCreated(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity) {onUpdate();}

    /**
     * Called when a node in this graph is destroyed.
     *
     * @param node         the node destroyed.
     * @param nodeEntity   the node's entity, if any.
     * @param linkEntities any link entities that were removed.
     */
    default void onNodeDestroyed(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity,
                                 Map<LinkPos, LinkEntity> linkEntities) {onUpdate();}

    /**
     * Called when two nodes in the graph are linked.
     *
     * @param a      the first node in the link.
     * @param b      the second node in the link.
     * @param entity the link entity that was added, if any.
     */
    default void onLink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b,
                        @Nullable LinkEntity entity) {onUpdate();}

    /**
     * Called when two nodes in the graph ar unlinked.
     *
     * @param a      the first node in the link.
     * @param b      the second node in the link.
     * @param entity the link entity that was removed, if any.
     */
    default void onUnlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b,
                          @Nullable LinkEntity entity) {onUpdate();}

    /**
     * Called by the default implementations of the node update methods.
     */
    default void onUpdate() {}

    /**
     * Called when this graph's graph world is ticked, if this graph is loaded.
     */
    default void onTick() {}

    /**
     * Merges another graph entity into this one.
     *
     * @param other the graph entity to be merged into this one.
     */
    void merge(@NotNull G other);
}
