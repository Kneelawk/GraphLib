package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Context for node entities.
 * <p>
 * It is safe to hold on to this object for a node entity's lifetime, as all contained references should be valid for
 * that long.
 */
public interface NodeEntityContext {
    /**
     * Marks this node entity's graph as dirty, so that this node entity can be re-written to NBT.
     */
    void markDirty();

    /**
     * Gets the block node holder associated with this node entity.
     *
     * @return the block node holder associated with this entity.
     */
    @NotNull NodeHolder<BlockNode> getHolder();

    /**
     * Gets the block world that this node entity exists in.
     *
     * @return the block world that this node entity exists in.
     */
    @NotNull ServerWorld getBlockWorld();

    /**
     * Gets a view of the graph world this node entity exists in.
     *
     * @return a view of the graph world this node entity exists in.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the block position of this node entity.
     *
     * @return the block position of thie node entity.
     */
    default @NotNull BlockPos getPos() {
        return getHolder().getPos();
    }

    /**
     * Gets the block node associated with this node entity.
     *
     * @return the block node associated with this node entity.
     */
    default @NotNull BlockNode getNode() {
        return getHolder().getNode();
    }

    /**
     * Gets the graph id of the graph this node entity is in.
     *
     * @return the graph id of the graph this node entity is in.
     */
    default long getGraphId() {
        return getHolder().getGraphId();
    }

    /**
     * Converts this node entity context to a node context for colling methods on this node entity's associated block
     * node.
     *
     * @return this node entity context as a node context.
     */
    default @NotNull NodeContext toNodeContext() {
        return new NodeContext(getHolder(), getBlockWorld(), getGraphWorld());
    }

    /**
     * Gets the block state at this node entity's position.
     *
     * @return the block state at this node entity's position.
     */
    default @NotNull BlockState getBlockState() {
        return getBlockWorld().getBlockState(getPos());
    }

    /**
     * Gets the block entity at this node entity's position, if any.
     *
     * @return the block entity at this node entity's position, if any.
     */
    default @Nullable BlockEntity getBlockEntity() {
        return getBlockWorld().getBlockEntity(getPos());
    }
}
