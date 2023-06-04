package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;

/**
 * Context for link entities.
 * <p>
 * It is safe to hold on to this object for the entity's lifetime, as all its contained references should be valid for
 * that long.
 */
public interface LinkEntityContext {
    /**
     * Marks this link entity's graph as dirty, so that this link entity can be re-written to NBT.
     */
    void markDirty();

    /**
     * Gets the link holder associated with this link entity.
     *
     * @return the link holder associated with this link entity.
     */
    @NotNull LinkHolder<LinkKey> getHolder();

    /**
     * Gets the world of blocks that this link entity exists within.
     *
     * @return the world of blocks that this link entity exists within.
     */
    @NotNull ServerWorld getBlockWorld();

    /**
     * Gets the world of graphs that this link entity exists within.
     *
     * @return the world of graphs that this link entity exists within.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the holder for the first node in this link entity's link.
     *
     * @return the holder for the first node in this link entity's link.
     */
    default @NotNull NodeHolder<BlockNode> getFirst() {
        return getHolder().getFirst();
    }

    /**
     * Gets the holder for the second node in this link entity's link.
     *
     * @return the holder for the second node in this link entity's link.
     */
    default @NotNull NodeHolder<BlockNode> getSecond() {
        return getHolder().getSecond();
    }

    /**
     * Gets the graph id of the graph this link is in.
     *
     * @return the graph id of the graph this link is in.
     */
    default long getGraphId() {
        return getHolder().getGraphId();
    }

    /**
     * Gets the block position of the first node in this link entity's link.
     *
     * @return the block position of the first node in this link entity's link.
     */
    default @NotNull BlockPos getFirstBlockPos() {
        return getHolder().getFirst().getPos();
    }

    /**
     * Gets the block position of the second node in the link entity's link.
     *
     * @return the block position of the second node in the link entity's link.
     */
    default @NotNull BlockPos getSecondBlockPos() {
        return getHolder().getSecond().getPos();
    }

    /**
     * Gets the block state of the first node in this link.
     *
     * @return the block state of the first node in this link.
     */
    default @NotNull BlockState getFirstBlockState() {
        return getBlockWorld().getBlockState(getFirstBlockPos());
    }

    /**
     * Gets the block state of the second node in this link.
     *
     * @return the block state of the second node in this link.
     */
    default @NotNull BlockState getSecondBlockState() {
        return getBlockWorld().getBlockState(getSecondBlockPos());
    }

    /**
     * Gets the block entity at the first node in this link.
     *
     * @return the block entity at the first node in this link.
     */
    default @Nullable BlockEntity getFirstBlockEntity() {
        return getBlockWorld().getBlockEntity(getFirstBlockPos());
    }

    /**
     * Gets the block entity at the second node in this link.
     *
     * @return the block entity at the second node in this link.
     */
    default @Nullable BlockEntity getSecondBlockEntity() {
        return getBlockWorld().getBlockEntity(getSecondBlockPos());
    }

    /**
     * Gets the first block node in this link.
     *
     * @return the first block node in this link.
     */
    default @NotNull BlockNode getFirstNode() {
        return getHolder().getFirst().getNode();
    }

    /**
     * Gets the second block node in this link.
     *
     * @return the second block node in this link.
     */
    default @NotNull BlockNode getSecondNode() {
        return getHolder().getSecond().getNode();
    }

    /**
     * Gets the node entity for the first node in this link.
     *
     * @return the node entity for the first node in this link.
     */
    default @Nullable NodeEntity getFirstNodeEntity() {
        BlockGraph graph = getGraphWorld().getGraph(getHolder().getFirst().getGraphId());
        if (graph != null) {
            return graph.getNodeEntity(getHolder().getFirst().toNodePos());
        }
        return null;
    }

    /**
     * Gets the node entity for the first node in this link, if the correct type.
     *
     * @param entityClass the class of the node entity to get.
     * @param <T>         the type of the node entity to get.
     * @return the node entity for the first node in this link, if the correct type.
     */
    default <T extends NodeEntity> @Nullable T getFirstNodeEntity(Class<T> entityClass) {
        NodeEntity entity = getFirstNodeEntity();
        if (entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }

    /**
     * Gets the node entity for the second node in this link.
     *
     * @return the node entity for the second node in this link.
     */
    default @Nullable NodeEntity getSecondNodeEntity() {
        BlockGraph graph = getGraphWorld().getGraph(getHolder().getSecond().getGraphId());
        if (graph != null) {
            return graph.getNodeEntity(getHolder().getSecond().toNodePos());
        }
        return null;
    }

    /**
     * Gets the node entity for the second node in this link, if the correct type.
     *
     * @param entityClass the class of the node entity to get.
     * @param <T>         the type of the node entity to get.
     * @return the node entity for the second node in this link, if the correct type.
     */
    default <T extends NodeEntity> @Nullable T getSecondNodeEntity(Class<T> entityClass) {
        NodeEntity entity = getSecondNodeEntity();
        if (entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }
}
