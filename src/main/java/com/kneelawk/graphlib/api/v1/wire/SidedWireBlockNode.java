package com.kneelawk.graphlib.api.v1.wire;

import com.kneelawk.graphlib.api.v1.graph.BlockNodeHolder;
import com.kneelawk.graphlib.api.v1.node.SidedBlockNode;
import com.kneelawk.graphlib.api.v1.util.graph.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * A block node wire that occupies the side of a block.
 * <p>
 * An example of a node like this would be a red-alloy wire from Wired Redstone.
 */
public interface SidedWireBlockNode extends SidedBlockNode {
    /**
     * Checks whether this sided block node can connect to the given other block node.
     *
     * @param world          the block world that both nodes are in.
     * @param pos            the block-position that this node is at.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param self           the block node holder associated with this node.
     * @param other          the other block node.
     * @return <code>true</code> if a connection should be allowed to form, <code>false</code> otherwise.
     */
    default boolean canConnect(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Direction inDirection,
                               @NotNull WireConnectionType connectionType, @NotNull Node<BlockNodeHolder> self,
                               @NotNull Node<BlockNodeHolder> other) {
        return true;
    }
}
