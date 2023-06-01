package com.kneelawk.graphlib.api.graph;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.LinkEntityFactory;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;

/**
 * Holds and manages all block graphs for a given world.
 */
public interface GraphWorld extends GraphView {

    /**
     * Connects two nodes to each other.
     * <p>
     * Note: in order for manually connected links to not be removed when the connected nodes are updated,
     * {@link LinkKey#isAutomaticRemoval(LinkContext)} should return <code>false</code> for the given key.
     *
     * @param a   the first node to be connected.
     * @param b   the second node to be connected.
     * @param key the key of the connection.
     * @return the link created, or <code>null</code> if no link could be created.
     */
    default @Nullable LinkHolder<LinkKey> connectNodes(NodePos a, NodePos b, LinkKey key) {
        return connectNodes(a, b, key, key::createLinkEntity);
    }

    /**
     * Connects two nodes to each other.
     * <p>
     * Note: in order for manually connected links to not be removed when the connected nodes are updated,
     * {@link LinkKey#isAutomaticRemoval(LinkContext)} should return <code>false</code> for the given key.
     *
     * @param a             the first node to be connected.
     * @param b             the second node to be connected.
     * @param key           the key of the connection.
     * @param entityFactory a factory for potentially creating the link's entity.
     * @return the link created, or <code>null</code> if no link could be created.
     */
    @Nullable LinkHolder<LinkKey> connectNodes(NodePos a, NodePos b, LinkKey key, LinkEntityFactory entityFactory);

    /**
     * Disconnects two nodes from each other.
     *
     * @param a   the first node to be disconnected.
     * @param b   the second node to be disconnected.
     * @param key the key of the connection.
     * @return <code>true</code> if a link was actually removed, <code>false</code> otherwise.
     */
    boolean disconnectNodes(NodePos a, NodePos b, LinkKey key);

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
