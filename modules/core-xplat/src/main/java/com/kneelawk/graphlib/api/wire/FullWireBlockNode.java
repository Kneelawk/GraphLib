package com.kneelawk.graphlib.api.wire;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * A block node wire that occupies a full block instead of sitting on the side of a block.
 * <p>
 * This is usually what most non-gate machines are.
 */
public interface FullWireBlockNode extends BlockNode {
    /**
     * Checks whether this block node can connect to the given other block node.
     *
     * @param self     this node's holder and context.
     * @param onSide   the side of this block node that the other node is trying to connect to.
     * @param wireSide the side of the block that the connecting wire is at, or <code>null</code> if the wire is a full
     *                 block or otherwise non-sided.
     * @param link     the link to the block node that could possibly connect to this node.
     * @return <code>true</code> if this node and the other node should be allowed to connect, <code>false</code>
     * otherwise.
     */
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull Direction onSide,
                               @Nullable Direction wireSide, @NotNull HalfLink link) {
        return true;
    }

    /**
     * Default find connections implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#fullBlockFindConnections(FullWireBlockNode, NodeHolder)}.
     *
     * @param self this node's holder, holding the context of this node.
     * @return all the connections found through the default implementation.
     */
    @Override
    default @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
        return WireConnectionDiscoverers.fullBlockFindConnections(this, self);
    }

    /**
     * Default can connect implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#fullBlockCanConnect(FullWireBlockNode, NodeHolder, HalfLink)}.
     *
     * @param self  this node's holder, holding the context of this node.
     * @param other the other node to attempt to connect to.
     * @return whether this node can connect, as per the default implementation.
     */
    @Override
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return WireConnectionDiscoverers.fullBlockCanConnect(this, self, other);
    }
}
