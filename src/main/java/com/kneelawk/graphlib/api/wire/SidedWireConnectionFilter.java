package com.kneelawk.graphlib.api.wire;

import com.kneelawk.graphlib.api.graph.BlockNodeHolder;
import com.kneelawk.graphlib.api.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * Allows an external object to filter the connections connecting to a sided wire block node.
 */
public interface SidedWireConnectionFilter {
    /**
     * Checks whether this filter allows two block nodes to connect.
     *
     * @param self           the node that the check is with respect to.
     * @param world          the block world that both nodes are in.
     * @param pos            the block-position of the <code>self</code> node.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param otherNode      the other block node.
     * @return <code>true</code> if the two block nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                       @NotNull Direction inDirection, @NotNull WireConnectionType connectionType,
                       @NotNull Node<BlockNodeHolder> selfNode, @NotNull Node<BlockNodeHolder> otherNode);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default SidedWireConnectionFilter and(@NotNull SidedWireConnectionFilter otherFilter) {
        return (self, world, pos, inDirection, connectionType, selfNode, otherNode) ->
                canConnect(self, world, pos, inDirection, connectionType, selfNode, otherNode) &&
                        otherFilter.canConnect(self, world, pos, inDirection, connectionType, selfNode, otherNode);
    }
}
