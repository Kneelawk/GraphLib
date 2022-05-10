package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Interface that allows access to the nodes at given positions.
 */
public interface NodeView {
    /**
     * Gets the nodes in the given block-position.
     *
     * @param pos the block-position to get the nodes in.
     * @return a stream of all the nodes in the given block-position.
     */
    @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull BlockPos pos);

    /**
     * Gets the nodes in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in the given sided block-position.
     */
    @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull SidedPos pos);
}
