package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * Allows an external object to filter the connections connecting to a center wire block node.
 */
public interface CenterWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self   the node that this check is with respect to.
     * @param holder the node's holder.
     * @param onSide the side of this block that the other node is trying to connect to.
     * @param link   the link to the block node holder holding the other node.
     * @return <code>true</code> if the two nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull CenterWireBlockNode self, @NotNull NodeHolder<BlockNode> holder,
                       @NotNull Direction onSide, @NotNull HalfLink link);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default @NotNull CenterWireConnectionFilter and(@NotNull CenterWireConnectionFilter otherFilter) {
        return (self, holder, onSide, link) -> canConnect(self, holder, onSide, link) &&
            otherFilter.canConnect(self, holder, onSide, link);
    }
}
