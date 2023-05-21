package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * The context that is passed to a block node during construction.
 * <p>
 * This is safe to store inside the constructed block node, as everything referenced should remain valid for the node's
 * lifetime.
 */
public interface NodeContext {
    /**
     * Marks this node's graph as dirty, so
     */
    void markDirty();

    /**
     * Gets this node's block world.
     *
     * @return this node's block world.
     */
    @NotNull ServerWorld getBlockWorld();

    /**
     * Gets this node's graph world.
     *
     * @return this node's graph world.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the id of the last block graph that this context belonged to.
     *
     * @return the id of the last block graph that this context belonged to.
     */
    long getGraphId();

    /**
     * Gets the block graph that this node would belong to, if it exists.
     * <p>
     * Be careful <b>not</b> to store references to this, as which block graph a node is part of can change.
     *
     * @return the block graph that this node would belong to, if it exists.
     */
    @Nullable BlockGraph getGraph();

    /**
     * Gets this context's node holder.
     * <p>
     * Note: this is <code>null</code> before the block node has been fully initialized. By the time
     * {@link BlockNode#onInit()} is called, this will have been initialized.
     *
     * @return this context's node holder.
     */
    @Nullable NodeHolder<BlockNode> getSelf();

    /**
     * Gets this node's block position.
     *
     * @return this node's block position.
     */
    @NotNull BlockPos getPos();
}
