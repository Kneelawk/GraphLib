package com.kneelawk.graphlib.graph;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Positioned holder for a block node.
 * <p>
 * All block nodes are associated with a block-position and are wrapped in a block-node-holder that stores the position
 * information along with information about what graph the block node is a part of.
 */
public interface BlockNodeHolder {
    /**
     * Gets this block node holder's block position.
     *
     * @return the block-position of this block node holder.
     */
    @NotNull BlockPos getPos();

    /**
     * Gets the {@link BlockNode} this holder is holding.
     *
     * @return the BlockNode this holder is holding.
     */
    @NotNull BlockNode getNode();

    /**
     * Gets the graph id of the graph that this node is part of.
     *
     * @return the graph id of this node.
     */
    long getGraphId();
}
