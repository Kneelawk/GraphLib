package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * Allows an external object to filter the connections connecting to a sided wire block node.
 */
public interface SidedWireConnectionFilter {
    /**
     * Checks whether this filter allows two block nodes to connect.
     *
     * @param self           the node that the check is with respect to.
     * @param holder         the node's holder and context.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param link           the link to the other block node.
     * @return <code>true</code> if the two block nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull NodeHolder<BlockNode> holder,
                       @NotNull Direction inDirection, @NotNull WireConnectionType connectionType,
                       @NotNull HalfLink link);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default @NotNull SidedWireConnectionFilter and(@NotNull SidedWireConnectionFilter otherFilter) {
        return (self, holder, inDirection, connectionType, link) ->
            canConnect(self, holder, inDirection, connectionType, link) &&
                otherFilter.canConnect(self, holder, inDirection, connectionType, link);
    }
}
