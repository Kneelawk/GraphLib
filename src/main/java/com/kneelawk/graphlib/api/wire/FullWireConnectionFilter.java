package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * Allows an external object to filter the connections connecting to a full block wire block node.
 */
public interface FullWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self      the node that the check is with respect to.
     * @param selfNode  the node holder of the node that this check is with respect to.
     * @param world     the block world that both nodes are in.
     * @param onSide    the side of the <code>self</code> node that the other node would connect to.
     * @param wireSide  the side of its block the other node is at, or <code>null</code> if it is a full block or
     *                  otherwise un-sided.
     * @param otherNode the other node that the <code>self</code> node would connect to.
     * @return <code>true</code> if the two node should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull FullWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                       @NotNull ServerWorld world, @NotNull Direction onSide, @Nullable Direction wireSide,
                       @NotNull NodeHolder<BlockNode> otherNode);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default @NotNull FullWireConnectionFilter and(@NotNull FullWireConnectionFilter otherFilter) {
        return (self, selfNode, world, onSide, wireSide, otherNode) ->
            canConnect(self, selfNode, world, onSide, wireSide, otherNode) &&
                otherFilter.canConnect(self, selfNode, world, onSide, wireSide, otherNode);
    }
}
