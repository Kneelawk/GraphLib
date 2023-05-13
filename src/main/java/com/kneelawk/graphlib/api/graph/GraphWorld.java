package com.kneelawk.graphlib.api.graph;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.util.SidedPos;

/**
 * Holds and manages all block graphs for a given world.
 */
public interface GraphWorld extends GraphView {

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
