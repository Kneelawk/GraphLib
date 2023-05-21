package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * Allows an external object to filter the connections connecting to a center wire block node.
 */
public interface CenterWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self      the node that this check is with respect to.
     * @param ctx       the node context of the node that the check is with respect to.
     * @param onSide    the side of this block that the other node is trying to connect to.
     * @param otherNode the block node holder holding the other node.
     * @return <code>true</code> if the two nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull CenterWireBlockNode self, @NotNull NodeContext ctx, @NotNull Direction onSide,
                       @NotNull NodeHolder<BlockNode> otherNode);
}
