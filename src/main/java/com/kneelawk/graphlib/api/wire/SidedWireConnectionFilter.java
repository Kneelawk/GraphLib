package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * Allows an external object to filter the connections connecting to a sided wire block node.
 */
public interface SidedWireConnectionFilter {
    /**
     * Checks whether this filter allows two block nodes to connect.
     *
     * @param self           the node that the check is with respect to.
     * @param selfNode       the block node holder associated with this node.
     * @param world          the block world that both nodes are in.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param otherNode      the other block node.
     * @return <code>true</code> if the two block nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                       @NotNull ServerWorld world, @NotNull Direction inDirection,
                       @NotNull WireConnectionType connectionType, @NotNull NodeHolder<BlockNode> otherNode);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default SidedWireConnectionFilter and(@NotNull SidedWireConnectionFilter otherFilter) {
        return (self, selfNode, world, inDirection, connectionType, otherNode) ->
            canConnect(self, selfNode, world, inDirection, connectionType, otherNode) &&
                otherFilter.canConnect(self, selfNode, world, inDirection, connectionType, otherNode);
    }
}