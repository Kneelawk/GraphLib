package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * More general wire connection filter, designed only to determine if two types of block nodes should be allowed to
 * connect.
 */
public interface WireConnectionFilter
    extends FullWireConnectionFilter, SidedWireConnectionFilter, CenterWireConnectionFilter {
    /**
     * Checks whether this filter allows these two block nodes to connect.
     *
     * @param self  the block node that the check is performed with respect to.
     * @param other the other block node that could potentially be connected.
     * @return <code>true</code> if the two block nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean accepts(@NotNull BlockNode self, @NotNull BlockNode other);

    @Override
    default boolean canConnect(@NotNull FullWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                               @NotNull ServerWorld world, @NotNull Direction onSide, @Nullable Direction wireSide,
                               @NotNull NodeHolder<BlockNode> otherNode) {
        return accepts(self, otherNode.getNode());
    }

    @Override
    default boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                               @NotNull ServerWorld world, @NotNull Direction inDirection,
                               @NotNull WireConnectionType connectionType, @NotNull NodeHolder<BlockNode> otherNode) {
        return accepts(self, otherNode.getNode());
    }

    @Override
    default boolean canConnect(@NotNull CenterWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                               @NotNull ServerWorld world, @NotNull Direction onSide,
                               @NotNull NodeHolder<BlockNode> otherNode) {
        return accepts(self, otherNode.getNode());
    }
}