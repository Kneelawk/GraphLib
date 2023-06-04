package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * Allows an external object to filter the connections connecting to a full block wire block node.
 */
public interface FullWireConnectionFilter {
    /**
     * Checks whether this filter allows the two block nodes to connect.
     *
     * @param self     the node that the check is with respect to.
     * @param holder   the node's holder.
     * @param onSide   the side of the <code>self</code> node that the other node would connect to.
     * @param wireSide the side of its block the other node is at, or <code>null</code> if it is a full block or
     *                 otherwise un-sided.
     * @param link     the link to the other node that the <code>self</code> node would connect to.
     * @return <code>true</code> if the two node should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull FullWireBlockNode self, @NotNull NodeHolder<BlockNode> holder,
                       @NotNull Direction onSide, @Nullable Direction wireSide, @NotNull HalfLink link);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default @NotNull FullWireConnectionFilter and(@NotNull FullWireConnectionFilter otherFilter) {
        return (self, holder, onSide, wireSide, link) -> canConnect(self, holder, onSide, wireSide, link) &&
            otherFilter.canConnect(self, holder, onSide, wireSide, link);
    }
}
