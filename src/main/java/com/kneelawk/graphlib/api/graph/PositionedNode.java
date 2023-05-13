package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * Immutable version of {@link NodeHolder}, holding only a node and its position.
 * <p>
 * This can be useful if a set of nodes needs to be sent to another thread for processing.
 *
 * @param pos     the block position of the node.
 * @param node    the node itself.
 * @param graphId the id of the graph that this node belonged to when {@link NodeHolder#toPositionedNode()} was called.
 * @param <T>     the specific type of the node.
 */
public record PositionedNode<T extends BlockNode>(@NotNull BlockPos pos, @NotNull T node, long graphId) {
    /**
     * Creates a PositionedNode.
     *
     * @param pos     the block position of the node.
     * @param node    the node itself.
     * @param graphId the id of the graph that this node belonged to when {@link NodeHolder#toPositionedNode()} was called.
     */
    @ApiStatus.Internal
    public PositionedNode(@NotNull BlockPos pos, @NotNull T node, long graphId) {
        this.pos = pos.toImmutable();
        this.node = node;
        this.graphId = graphId;
    }
}
