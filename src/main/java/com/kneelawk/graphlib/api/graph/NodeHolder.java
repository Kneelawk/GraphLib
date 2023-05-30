package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.NodePos;

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
     * @return a collection of all the {@link LinkHolder}s this node has with other nodes.
     */
    @NotNull Collection<LinkHolder<LinkKey>> getConnections();

    /**
     * Gets all connections with keys of the given type.
     *
     * @param keyClass the class of the key to filter by.
     * @param <K>      the type of the key to filter by.
     * @return all connections with keys of the given type.
     */
    @NotNull <K extends LinkKey> Stream<LinkHolder<K>> getConnectionsOfType(Class<K> keyClass);

    /**
     * Gets all the connections with keys that match the given predicate.
     *
     * @param keyClass the class of the key to filter by.
     * @param filter   predicate to filter keys by.
     * @param <K>      the type of key to filter by.
     * @return all connections with keys that match the given predicate.
     */
    @NotNull <K extends LinkKey> Stream<LinkHolder<K>> getConnectionsThatMatch(Class<K> keyClass,
                                                                               Predicate<K> filter);

    /**
     * Gets an immutable view of this node holder's position and node.
     *
     * @return a positioned node containing this holder's position and node.
     */
    @NotNull SnapshotNode<T> toSnapshot();

    /**
     * Gets the node pos of this node holder, holding only the block position and block node.
     *
     * @return the node pos of this node holder.
     */
    @NotNull NodePos toNodePos();

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
