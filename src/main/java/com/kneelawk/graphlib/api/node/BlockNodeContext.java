package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeHolder;

/**
 * The context that is passed to a block node during construction.
 * <p>
 * This is safe to store inside the constructed block node, as everything referenced should remain valid for the node's
 * lifetime.
 */
public interface BlockNodeContext {
    /**
     * Marks this node's graph as dirty, so
     */
    void markDirty();

    /**
     * Gets this node's block world.
     *
     * @return this node's block world.
     */
    ServerWorld getBlockWorld();

    /**
     * Gets this node's graph world.
     *
     * @return this node's graph world.
     */
    GraphWorld getGraphWorld();

    /**
     * Gets the id of the last block graph that this context belonged to.
     *
     * @return the id of the last block graph that this context belonged to.
     */
    long getGraphId();

    /**
     * Gets the block graph that this node would belong to, if it exists.
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
    BlockPos getPos();
}