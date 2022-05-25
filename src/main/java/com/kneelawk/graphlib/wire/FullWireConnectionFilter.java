package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows an external object to filter the connections connecting to a full block wire block node.
 */
public interface FullWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self     the node that the check is with respect to.
     * @param world    the block world that both nodes are in.
     * @param pos      the position of the <code>self</code> node.
     * @param onSide   the side of the <code>self</code> node that the other node would connect to.
     * @param wireSide the side of its block the other node is at, or <code>null</code> if it is a full block or
     *                 otherwise un-sided.
     * @param other    the other node that the <code>self</code> node would connect to.
     * @return <code>true</code> if the two node should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull FullWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                       @NotNull Direction onSide, @Nullable Direction wireSide,
                       @NotNull Node<BlockNodeHolder> other);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default FullWireConnectionFilter and(@NotNull FullWireConnectionFilter otherFilter) {
        return (self, world, pos, onSide, wireSide, other) -> canConnect(self, world, pos, onSide, wireSide, other) &&
                otherFilter.canConnect(self, world, pos, onSide, wireSide, other);
    }
}
