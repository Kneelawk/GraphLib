package com.kneelawk.graphlib.api.v1.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.v1.graph.NodeHolder;
import com.kneelawk.graphlib.api.v1.util.graph.Node;

/**
 * Allows an external object to filter the connections connecting to a center wire block node.
 */
public interface CenterWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self      the node that this check is with respect to.
     * @param world     the block world that both nodes are in.
     * @param pos       the block position of this node.
     * @param onSide    the side of this block that the other node is trying to connect to.
     * @param selfNode  the block node holder associated with this block node.
     * @param otherNode the block node holder holding the other node.
     * @return <code>true</code> if the two nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull CenterWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                       @NotNull Direction onSide, @NotNull Node<NodeHolder> selfNode,
                       @NotNull Node<NodeHolder> otherNode);
}
