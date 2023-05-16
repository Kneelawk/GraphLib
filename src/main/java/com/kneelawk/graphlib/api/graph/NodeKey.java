package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

/**
 * Holds all comparable information about a block-node.
 * <p>
 * This is the type that node lookups are performed against.
 *
 * @param pos        the block position of the node.
 * @param uniqueData any extra data a node wishes to use to make itself unique.
 */
public record NodeKey(@NotNull BlockPos pos, @NotNull Object uniqueData) {
    /**
     * Creates a NodeKey.
     *
     * @param pos        the block-position of the node. Note, this is made immutable.
     * @param uniqueData the unique data associated with this node.
     */
    public NodeKey(@NotNull BlockPos pos, Object uniqueData) {
        this.pos = pos.toImmutable();
        this.uniqueData = uniqueData;
    }
}
