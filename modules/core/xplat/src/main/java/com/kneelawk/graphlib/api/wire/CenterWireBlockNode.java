package com.kneelawk.graphlib.api.wire;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * A block node that sits, hovering in the center of the block but without taking up the entire block space.
 */
public interface CenterWireBlockNode extends BlockNode {
    /**
     * Checks whether this block node can connect to the given other block node.
     *
     * @param self   this node's holder and context.
     * @param onSide the side of this block that the other node is trying to connect to.
     * @param link   the link to the block node holder holding the other node.
     * @return <code>true</code> if this node and the other node should be allowed to connect, <code>false</code>
     * otherwise.
     */
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull Direction onSide,
                               @NotNull HalfLink link) {
        return true;
    }

    /**
     * Default find connections implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#centerWireFindConnections(CenterWireBlockNode, NodeHolder)}.
     *
     * @param self this node's holder, holding the context of this node.
     * @return all the connections found through the default implementation.
     */
    @Override
    default @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
        return WireConnectionDiscoverers.centerWireFindConnections(this, self);
    }

    /**
     * Default can connect implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#centerWireCanConnect(CenterWireBlockNode, NodeHolder, HalfLink)}.
     *
     * @param self  this node's holder, holding the context of this node.
     * @param other the other node to attempt to connect to.
     * @return whether this node can connect, as per the default implementation.
     */
    @Override
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return WireConnectionDiscoverers.centerWireCanConnect(this, self, other);
    }
}
