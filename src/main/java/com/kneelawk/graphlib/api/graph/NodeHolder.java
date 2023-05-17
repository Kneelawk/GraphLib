package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.NodeKey;

/**
 * Positioned holder for a block node.
 * <p>
 * All block nodes are associated with a block-position and are wrapped in a block-node-holder that stores the position
 * information along with information about what graph the block node is a part of.
 *
 * @param <T> the type of node this holder is holding.
 */
public interface NodeHolder<T extends BlockNode> {
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
    @NotNull T getNode();

    /**
     * Gets the graph id of the graph that this node is part of.
     *
     * @return the graph id of this node.
     */
    long getGraphId();

    /**
     * Gets all the connections this node has with other nodes.
     *
     * @return a collection of all the {@link NodeConnection}s this node has with other nodes.
     */
    @NotNull Map<NodeKey, NodeConnection> getConnections();

    /**
     * Gets an immutable view of this node holder's position and node.
     *
     * @return a positioned node containing this holder's position and node.
     */
    @NotNull PositionedNode<T> toPositionedNode();

    /**
     * Gets an immutable, unique, comparable view of this node holder's position and data that can be used to look up
     * this node-holder.
     *
     * @return this holder's key.
     */
    default @NotNull NodeKey toNodeKey() {
        return new NodeKey(getPos(), getNode().getUniqueData());
    }

    /**
     * Checks whether the contained node can be cast to the new type.
     * <p>
     * Tool to get around Java's broken type-variance system.
     *
     * @param newType the type the internal node is to be cast to.
     * @return <code>true</code> if the internal node is of the correct type to be cast.
     */
    boolean canCast(Class<?> newType);

    /**
     * Casts the internal node to the given type.
     * <p>
     * Tool to get around Java's broken type-variance system.
     *
     * @param newType the type the internal node is to be cast to.
     * @param <R>     the type the internal node is to be cast to.
     * @return a node-holder containing the new type.
     * @throws ClassCastException if the cast failed.
     */
    <R extends BlockNode> NodeHolder<R> cast(Class<R> newType) throws ClassCastException;
}
