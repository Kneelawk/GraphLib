package com.kneelawk.graphlib.api.wire;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
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
     * @param self           this node's holder and context.
     * @param inDirection    the direction that the other node is connecting from.
     * @param connectionType the type of connection that would be formed.
     * @param link           the link to the other block node.
     * @return <code>true</code> if a connection should be allowed to form, <code>false</code> otherwise.
     */
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull Direction inDirection,
                               @NotNull WireConnectionType connectionType, @NotNull HalfLink link) {
        return true;
    }

    /**
     * Default find connections implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#sidedWireFindConnections(SidedWireBlockNode, NodeHolder)}.
     *
     * @param self this node's holder, holding the context of this node.
     * @return all the connections found through the default implementation.
     */
    @Override
    default @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, self);
    }

    /**
     * Default can connect implementation.
     * <p>
     * This makes use of {@link WireConnectionDiscoverers#sidedWireCanConnect(SidedWireBlockNode, NodeHolder, HalfLink)}.
     *
     * @param self  this node's holder, holding the context of this node.
     * @param other the other node to attempt to connect to.
     * @return whether this node can connect, as per the default implementation.
     */
    @Override
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, self, other);
    }
}
