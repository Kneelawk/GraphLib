package com.kneelawk.graphlib.api.v1.node;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Used to get the {@link BlockNode}s that a block <b>should</b> have.
 * <p>
 * Nodes that are discovered here are usually created by the block or block-entity present at the given location.
 * These nodes are then compared, using their <code>hashCode()</code> and <code>equals()</code> functions, to the
 * nodes already in the controller's graphs at the given location and used to make adjustments if necessary (creating or
 * destroying connections, nodes, or graphs).
 */
public interface BlockNodeDiscoverer {
    /**
     * Gets the {@link BlockNode}s that the given block <b>should</b> have.
     *
     * @param world the world to check in.
     * @param pos   the position to check at.
     * @return all the {@link BlockNode}s that should be here.
     */
    @NotNull Collection<BlockNode> getNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);
}
