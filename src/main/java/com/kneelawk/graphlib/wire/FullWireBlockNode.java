package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block node wire that occupies a full block instead of sitting on the side of a block.
 * <p>
 * This is usually what most non-gate machines are.
 */
public interface FullWireBlockNode extends BlockNode {
    /**
     * Checks whether this block node can connect to the given other block node.
     *
     * @param world    the block world that both nodes are in.
     * @param pos      the position of this block node.
     * @param onSide   the side of this block node that the other node is trying to connect to.
     * @param wireSide the side of the block that the connecting wire is at, or <code>null</code> if the wire is a full
     *                 block or otherwise non-sided.
     * @param self     the block node holder associated with this block node.
     * @param other    the block node that could possibly connect to this node.
     * @return <code>true</code> if this node and the other node should be allowed to connect, <code>false</code>
     * otherwise.
     */
    default boolean canConnect(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Direction onSide,
                               @Nullable Direction wireSide, @NotNull Node<BlockNodeHolder> self,
                               @NotNull Node<BlockNodeHolder> other) {
        return true;
    }
}
