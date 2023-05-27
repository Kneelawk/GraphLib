package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.NodeHolder;

/**
 * Arbitrary data that can be stored in a graph.
 */
public interface GraphEntity<G extends GraphEntity<G>> {
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
     * Called right before this entity's associated graph is deleted.
     */
    void onDestroy();

    /**
     * Called right before this entity's associated graph is unloaded.
     * <p>
     * Note: This cannot cancel graph unloading, however, any changes made here will be saved.
     */
    void onUnload();

    /**
     * Called when a new node is added to the graph.
     *
     * @param node       the new node added to the graph.
     * @param nodeEntity the node's entity, if any.
     */
    void onNodeCreated(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity);

    /**
     * Called when a node in this graph is destroyed.
     *
     * @param node       the node destroyed.
     * @param nodeEntity the node's entity, if any.
     */
    void onNodeDestroyed(@NotNull NodeHolder<BlockNode> node, @Nullable NodeEntity nodeEntity);

    /**
     * Called when two nodes in the graph are linked.
     *
     * @param a the first node in the link.
     * @param b the second node in the link.
     */
    void onLink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b);

    /**
     * Called when two nodes in the graph ar unlinked.
     *
     * @param a the first node in the link.
     * @param b the second node in the link.
     */
    void onUnlink(@NotNull NodeHolder<BlockNode> a, @NotNull NodeHolder<BlockNode> b);

    /**
     * Merges another graph entity into this one.
     *
     * @param other the graph entity to be merged into this one.
     */
    void merge(@NotNull G other);
}
