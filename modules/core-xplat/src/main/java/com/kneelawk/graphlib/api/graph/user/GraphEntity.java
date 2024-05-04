package com.kneelawk.graphlib.api.graph.user;

import java.util.Map;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;

/**
 * Arbitrary data that can be stored in a graph.
 *
 * @param <G> this graph entity class.
 */
public interface GraphEntity<G extends GraphEntity<G>> {
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
    @Nullable Tag toTag();

    /**
     * Called right before this entity's associated graph is deleted.
     * <p>
     * Syncing note: This is called after all nodes have been removed, meaning that client-side versions of this graph
     * will already be gone.
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
     * Called before a new node is created on this graph.
     * <p>
     * This cannot be canceled.
     *
     * @param node   the node position that the new node will be at.
     * @param entity the entity that will be added to the new node, if any entity is present.
     */
    default void onPreNodeCreated(@NotNull NodePos node, @Nullable NodeEntity entity) {}

    /**
     * Called after a new node is created in this graph, as soon as the node is valid.
     * <p>
     * Note: often adding a new node to this graph will first involve creating a new graph for that node and then
     * merging that graph into this one.
     * <p>
     * Syncing note: this is called just after the {@code NODE_ADD} message has been sent to the client.
     *
     * @param node       the new node added to the graph.
     * @param nodeEntity the node's entity, if any.
     */
    default void onPostNodeCreated(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity) {onUpdate();}

    /**
     * Called before a node in this graph is destroyed.
     * <p>
     * This cannot be canceled.
     * <p>
     * Syncing note: this is called just before the {@code NODE_REMOVE} message is sent to the client.
     *
     * @param node the node that is about to be destroyed.
     */
    default void onPreNodeDestroyed(@NotNull NodeHolder<BlockNode> node) {onUpdate();}

    /**
     * Called after a node in this graph is destroyed.
     * <p>
     * Syncing note: this is called after the {@code NODE_REMOVE} message is sent to the client. If this node was the
     * last node in this graph entity's graph, then the associated graph will no longer exist on the client.
     *
     * @param node         the node destroyed.
     * @param nodeEntity   the node's entity, if any.
     * @param linkEntities any link entities that were removed.
     */
    default void onPostNodeDestroyed(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity,
                                     Map<LinkPos, LinkEntity> linkEntities) {}

    /**
     * Called before a new link between nodes in this graph is created.
     * <p>
     * This cannot be canceled.
     * <p>
     * Syncing note: this ic called just before the {@code LINK} message is sent to the client.
     *
     * @param link   the link to be created.
     * @param entity the link's entity, if any.
     */
    default void onPreLink(@NotNull LinkPos link, @Nullable LinkEntity entity) {}

    /**
     * Called after two nodes in the graph are linked, as soon as the link is valid.
     * <p>
     * Syncing note: this is called just after the {@code LINK} message has been sent the client.
     *
     * @param a      the first node in the link.
     * @param b      the second node in the link.
     * @param entity the link entity that was added, if any.
     */
    default void onPostLink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b,
                            @Nullable LinkEntity entity) {onUpdate();}

    /**
     * Called before two nodes in the graph are unlinked.
     * <p>
     * This cannot be canceled.
     * <p>
     * Syncing note: this is called just before the {@code UNLINK} message is sent to the client.
     *
     * @param link the link that is about to be destroyed.
     */
    default void onPreUnlink(@NotNull LinkHolder<LinkKey> link) {onUpdate();}

    /**
     * Called after two nodes in the graph ar unlinked.
     * <p>
     * Syncing note: This is called just before the {@code UNLINK} message is sent to the client.
     *
     * @param a      the first node in the link.
     * @param b      the second node in the link.
     * @param entity the link entity that was removed, if any.
     */
    default void onPostUnlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b,
                              @Nullable LinkEntity entity) {}

    /**
     * Called during an update when this graph entity's graph is in a valid state.
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
