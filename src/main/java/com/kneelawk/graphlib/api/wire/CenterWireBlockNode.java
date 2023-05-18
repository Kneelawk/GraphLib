package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * A block node that sits, hovering in the center of the block but without taking up the entire block space.
 */
public interface CenterWireBlockNode extends BlockNode {
    /**
     * Checks whether this block node can connect to the given other block node.
     *
     * @param self   the block node holder associated with this block node.
     * @param world  the block world that both nodes are in.
     * @param onSide the side of this block that the other node is trying to connect to.
     * @param other  the block node holder holding the other node.
     * @return <code>true</code> if this node and the other node should be allowed to connect, <code>false</code>
     * otherwise.
     */
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull ServerWorld world,
                               @NotNull Direction onSide, @NotNull NodeHolder<BlockNode> other) {
        return true;
    }
}