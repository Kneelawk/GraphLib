package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * A block node wire that occupies the side of a block.
 * <p>
 * An example of a node like this would be a red-alloy wire from Wired Redstone.
 */
public interface SidedWireBlockNode extends SidedBlockNode {
    /**
     * Checks whether this sided block node can connect to the given other block node.
     *
     * @param ctx            the node context for this node.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param link           the link to the other block node.
     * @return <code>true</code> if a connection should be allowed to form, <code>false</code> otherwise.
     */
    default boolean canConnect(@NotNull NodeContext ctx, @NotNull Direction inDirection,
                               @NotNull WireConnectionType connectionType, @NotNull HalfLink link) {
        return true;
    }
}
