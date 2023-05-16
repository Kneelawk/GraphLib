package com.kneelawk.graphlib.api.node;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

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
    @NotNull Collection<Discovery> getNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);

    /**
     * Represents a discovered block node.
     *
     * @param uniqueData  the node's unique data that, when combined with the block-pos, represents the node's key.
     * @param nodeCreator a creator for if a new node needs to be created as the given location.
     */
    record Discovery(UniqueData uniqueData, Supplier<BlockNode> nodeCreator) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Discovery discovery = (Discovery) o;
            return Objects.equals(uniqueData, discovery.uniqueData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uniqueData);
        }
    }
}
