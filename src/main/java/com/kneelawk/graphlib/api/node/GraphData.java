package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

/**
 * Describes user data stored in a block graph.
 * <p>
 * Do note, that under certain circumstances (e.g. Create contraptions, WorldEdit, etc.) this data can get deleted.
 * Effort is made to detect these deletions and call appropriate methods.
 */
public interface GraphData<G extends GraphData<G>> {
    /**
     * Encodes this data to a NBT tag.
     *
     * @return the NBT tag representing this graph data.
     */
    @Nullable NbtElement toTag();

    /**
     * Called right before this data's associated graph is deleted.
     */
    void onDelete();

    /**
     * Called right before this data's associated graph is unloaded.
     */
    void onUnload();

    /**
     * Merges another graph data into this one.
     *
     * @param other the graph data to merge into this one.
     */
    void merge(G other);
}
