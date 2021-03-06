package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Holds and manages all block graphs for a given world.
 */
public interface BlockGraphController extends NodeView {

    /**
     * Gets all nodes in the given block-position.
     *
     * @param pos the block-position to get nodes in.
     * @return a stream of the nodes in the given block-position.
     */
    @Override
    @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull BlockPos pos);

    /**
     * Gets all nodes in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of the nodes in the given sided block-position.
     */
    @Override
    @NotNull Stream<Node<BlockNodeHolder>> getNodesAt(@NotNull SidedPos pos);

    /**
     * Gets the IDs of all graph with nodes in the given block-position.
     *
     * @param pos the block-position to get the IDs of graphs with nodes at.
     * @return a stream of all the IDs of graphs with nodes in the given block-position.
     */
    @NotNull LongStream getGraphsAt(@NotNull BlockPos pos);

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

    /**
     * Gets the graph with the given ID.
     *
     * @param id the ID of the graph to get.
     * @return the graph with the given ID.
     */
    @Nullable
    BlockGraph getGraph(long id);

    /**
     * Called by the <code>/graphlib removeemptygraphs</code> command.
     * <p>
     * Removes all empty graphs. Graphs should never be empty, but it could theoretically happen if a mod isn't using
     * GraphLib correctly.
     *
     * @return the number of empty graphs removed.
     */
    int removeEmptyGraphs();
}
