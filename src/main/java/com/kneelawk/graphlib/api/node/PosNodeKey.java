package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

/**
 * Holds all comparable information about a block-node.
 * <p>
 * This is the type that node lookups are performed against.
 *
 * @param pos     the block position of the node.
 * @param nodeKey any extra data a node wishes to use to make itself unique.
 */
public record PosNodeKey(@NotNull BlockPos pos, @NotNull NodeKey nodeKey) {
    /**
     * Creates a NodeKey.
     *
     * @param pos     the block-position of the node. Note, this is made immutable.
     * @param nodeKey the unique data associated with this node.
     */
    public PosNodeKey(@NotNull BlockPos pos, NodeKey nodeKey) {
        this.pos = pos.toImmutable();
        this.nodeKey = nodeKey;
    }

    /**
     * Gets this pos's x component.
     *
     * @return this pos's x component.
     */
    public int getX() {
        return pos.getX();
    }

    /**
     * Gets this pos's y component.
     *
     * @return this pos's y component.
     */
    public int getY() {
        return pos.getY();
    }

    /**
     * Gets this pos's z component.
     *
     * @return this pos's z component.
     */
    public int getZ() {
        return pos.getZ();
    }
}
