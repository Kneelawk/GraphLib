package com.kneelawk.graphlib.api.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.HalfLink;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;

/**
 * Describes a connection between two nodes. May contain its own data, depending on implementation.
 *
 * @param <K> the type of key stored in this node link.
 */
public interface LinkHolder<K extends LinkKey> {
    /**
     * Gets the world of blocks that this link is associated with.
     *
     * @return the world of blocks that this link is associated with.
     */
    @NotNull World getBlockWorld();

    /**
     * Gets the world of graphs that this link is associated with.
     *
     * @return the world of graphs that this link is associated with.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the first node in this connection.
     *
     * @return the first node in this connection.
     */
    @NotNull NodeHolder<BlockNode> getFirst();

    /**
     * Gets the second node in this connection.
     *
     * @return the second node in this connection.
     */
    @NotNull NodeHolder<BlockNode> getSecond();

    /**
     * Gets the key of this link.
     *
     * @return the key of this link.
     */
    @NotNull K getKey();

    /**
     * Checks whether either node is the given node.
     *
     * @param node the node to check against.
     * @return <code>true</code> if either node matches the given node.
     */
    default boolean contains(@NotNull NodeHolder<BlockNode> node) {
        return Objects.equals(getFirst(), node) || Objects.equals(getSecond(), node);
    }

    /**
     * Gets the node opposite the given node.
     *
     * @param node the node to get the other end of the connection from.
     * @return the node at the other end of the connection from the given node.
     */
    default @NotNull NodeHolder<BlockNode> other(@NotNull NodeHolder<BlockNode> node) {
        NodeHolder<BlockNode> first = getFirst();
        if (Objects.equals(first, node)) {
            return getSecond();
        } else {
            return first;
        }
    }

    /**
     * Gets the node opposite the given node pos.
     *
     * @param node the node pos to get the other end of the connection from.
     * @return the node at the other end of the connection from the given node.
     */
    default @NotNull NodeHolder<BlockNode> other(@NotNull NodePos node) {
        NodeHolder<BlockNode> first = getFirst();
        if (Objects.equals(first.toNodePos(), node)) {
            return getSecond();
        } else {
            return first;
        }
    }

    /**
     * Gets the graph id of the graph this link is in.
     *
     * @return the graph id of the graph this link is in.
     */
    default long getGraphId() {
        return getFirst().getGraphId();
    }

    /**
     * Gets the block position of the first node in this link.
     *
     * @return the block position of the first node in this link.
     */
    default @NotNull BlockPos getFirstBlockPos() {
        return getFirst().getPos();
    }

    /**
     * Gets the block position of the second node in this link.
     *
     * @return the block position of the second node in this link.
     */
    default @NotNull BlockPos getSecondBlockPos() {
        return getSecond().getPos();
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
        return getFirst().getNode();
    }

    /**
     * Gets the second block node in this link.
     *
     * @return the second block node in this link.
     */
    default @NotNull BlockNode getSecondNode() {
        return getSecond().getNode();
    }

    /**
     * Gets the node entity for the first node in this link.
     *
     * @return the node entity for the first node in this link.
     */
    default @Nullable NodeEntity getFirstNodeEntity() {
        BlockGraph graph = getGraphWorld().getGraph(getFirst().getGraphId());
        if (graph != null) {
            return graph.getNodeEntity(getFirst().toNodePos());
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
        BlockGraph graph = getGraphWorld().getGraph(getSecond().getGraphId());
        if (graph != null) {
            return graph.getNodeEntity(getSecond().toNodePos());
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

    /**
     * Gets the link entity associated with this link.
     *
     * @return the link entity associated with this link.
     */
    default @Nullable LinkEntity getLinkEntity() {
        BlockGraph graph = getGraphWorld().getGraph(getFirst().getGraphId());
        if (graph != null) {
            return graph.getLinkEntity(toLinkPos());
        }
        return null;
    }

    /**
     * Gets the link entity associated with this link, if the correct type.
     *
     * @param entityClass the class of the entity to get.
     * @param <T>         the type of the entity to get.
     * @return the link entity associated with this link, if the correct type.
     */
    default <T extends LinkEntity> @Nullable T getLinkEntity(Class<T> entityClass) {
        LinkEntity entity = getLinkEntity();
        if (entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }

    /**
     * Represents this node link from the perspective of one of its nodes.
     *
     * @param perspective the node whose perspective from which this link is to be represented.
     * @return a keyed-node representing this link from the perspective of the given node.
     */
    default @NotNull HalfLink toHalfLink(@NotNull NodeHolder<BlockNode> perspective) {
        return new HalfLink(getKey(), other(perspective));
    }

    /**
     * Gets the link pos of this link, holding only node-positions and link key.
     *
     * @return the link pos of this link holder.
     */
    default @NotNull LinkPos toLinkPos() {
        return new LinkPos(getFirst().toNodePos(), getSecond().toNodePos(), getKey());
    }
}
