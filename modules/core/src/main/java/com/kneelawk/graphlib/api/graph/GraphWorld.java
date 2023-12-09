package com.kneelawk.graphlib.api.graph;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;

/**
 * Holds and manages all block graphs for a given world.
 */
public interface GraphWorld extends GraphView {
    /**
     * Gets the server world associated with this graph world.
     *
     * @return the server world associated with this graph world.
     */
    @Override
    @NotNull ServerWorld getWorld();

    /**
     * Adds a block node and optional node entity at the given position.
     *
     * @param pos  the block position of block node to be added.
     * @param node the node to be added.
     * @return the node created.
     */
    default @NotNull NodeHolder<BlockNode> addBlockNode(@NotNull BlockPos pos, @NotNull BlockNode node) {
        return addBlockNode(pos, node, null);
    }

    /**
     * Adds a block node and optional node entity at the given position.
     *
     * @param pos    the block position of block node to be added.
     * @param node   the node to be added.
     * @param entity the node's entity, if any.
     * @return the node created.
     */
    default @NotNull NodeHolder<BlockNode> addBlockNode(@NotNull BlockPos pos, @NotNull BlockNode node,
                                                        @Nullable NodeEntity entity) {
        return addBlockNode(new NodePos(pos, node), entity);
    }

    /**
     * Adds a block node and optional node entity at the given position.
     *
     * @param pos the position and block node to be added.
     * @return the node created.
     */
    default @NotNull NodeHolder<BlockNode> addBlockNode(@NotNull NodePos pos) {
        return addBlockNode(pos, null);
    }

    /**
     * Adds a block node and optional node entity at the given position.
     *
     * @param pos    the position and block node to be added.
     * @param entity the node's entity, if any.
     * @return the node created.
     */
    @NotNull NodeHolder<BlockNode> addBlockNode(@NotNull NodePos pos, @Nullable NodeEntity entity);

    /**
     * Removes a block node at a position.
     *
     * @param pos  the block position of the block node to be removed.
     * @param node the block node to be removed.
     * @return <code>true</code> if a node was actually removed, <code>false</code> otherwise.
     */
    default boolean removeBlockNode(@NotNull BlockPos pos, @NotNull BlockNode node) {
        return removeBlockNode(new NodePos(pos, node));
    }

    /**
     * Removes a block node at a position.
     *
     * @param pos the position and block node to be removed.
     * @return <code>true</code> if a node was actually removed, <code>false</code> otherwise.
     */
    boolean removeBlockNode(@NotNull NodePos pos);

    /**
     * Connects two nodes to each other.
     * <p>
     * Note: in order for manually connected links to not be removed when the connected nodes are updated,
     * {@link LinkKey#isAutomaticRemoval(LinkHolder)} should return <code>false</code> for the given key.
     *
     * @param a the first node to be connected.
     * @param b the second node to be connected.
     * @return the link created, or <code>null</code> if no link could be created.
     */
    default @Nullable LinkHolder<LinkKey> connectNodes(@NotNull NodePos a, @NotNull NodePos b) {
        return connectNodes(a, b, EmptyLinkKey.INSTANCE, null);
    }

    /**
     * Connects two nodes to each other.
     * <p>
     * Note: in order for manually connected links to not be removed when the connected nodes are updated,
     * {@link LinkKey#isAutomaticRemoval(LinkHolder)} should return <code>false</code> for the given key.
     *
     * @param a   the first node to be connected.
     * @param b   the second node to be connected.
     * @param key the key of the connection.
     * @return the link created, or <code>null</code> if no link could be created.
     */
    default @Nullable LinkHolder<LinkKey> connectNodes(@NotNull NodePos a, @NotNull NodePos b, @NotNull LinkKey key) {
        return connectNodes(a, b, key, null);
    }

    /**
     * Connects two nodes to each other.
     * <p>
     * Note: in order for manually connected links to not be removed when the connected nodes are updated,
     * {@link LinkKey#isAutomaticRemoval(LinkHolder)} should return <code>false</code> for the given key.
     *
     * @param a      the first node to be connected.
     * @param b      the second node to be connected.
     * @param key    the key of the connection.
     * @param entity the link's entity, if any.
     * @return the link created, or <code>null</code> if no link could be created.
     */
    @Nullable LinkHolder<LinkKey> connectNodes(@NotNull NodePos a, @NotNull NodePos b, @NotNull LinkKey key,
                                               @Nullable LinkEntity entity);

    /**
     * Disconnects two nodes from each other.
     *
     * @param a the first node to be disconnected.
     * @param b the second node to be disconnected.
     * @return <code>true</code> if a link was actually removed, <code>false</code> otherwise.
     */
    default boolean disconnectNodes(@NotNull NodePos a, @NotNull NodePos b) {
        return disconnectNodes(a, b, EmptyLinkKey.INSTANCE);
    }

    /**
     * Disconnects two nodes from each other.
     *
     * @param a   the first node to be disconnected.
     * @param b   the second node to be disconnected.
     * @param key the key of the connection.
     * @return <code>true</code> if a link was actually removed, <code>false</code> otherwise.
     */
    boolean disconnectNodes(@NotNull NodePos a, @NotNull NodePos b, @NotNull LinkKey key);

    /**
     * Notifies the controller that a block-position has been changed and may need to have its nodes and connections
     * recalculated.
     *
     * @param pos the changed block-position.
     */
    void updateNodes(@NotNull BlockPos pos);

    /**
     * Notifies the controller that a list of block-positions have been changed and may need to have their nodes and
     * connections recalculated.
     *
     * @param poses the iterable of all the block-positions that might have been changed.
     */
    void updateNodes(@NotNull Iterable<BlockPos> poses);

    /**
     * Notifies the controller that a list of block-positions have been changed and may need to have their nodes and
     * connections recalculated.
     *
     * @param posStream the stream ob all the block-positions that might have been changed.
     */
    void updateNodes(@NotNull Stream<BlockPos> posStream);

    /**
     * Updates the connections for all the nodes at the given block-position.
     *
     * @param pos the block-position of the nodes to update connections for.
     */
    void updateConnections(@NotNull BlockPos pos);

    /**
     * Updates the connections for all the nodes at the given sided block-position.
     *
     * @param pos the sided block-position of the nodes to update connections for.
     */
    void updateConnections(@NotNull SidedPos pos);
}
