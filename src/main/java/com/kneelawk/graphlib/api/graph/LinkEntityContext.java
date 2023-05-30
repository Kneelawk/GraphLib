package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Context for link entities.
 * <p>
 * It is safe to hold on to this object for the entity's lifetime, as all its contained references should be valid for
 * that long.
 */
public interface LinkEntityContext {
    /**
     * Marks this link entity's graph as dirty, so that this link entity can be re-written to NBT.
     */
    void markDirty();

    /**
     * Gets the link holder associated with this link entity.
     *
     * @return the link holder associated with this link entity.
     */
    @NotNull LinkHolder<LinkKey> getHolder();

    /**
     * Gets the world of blocks that this link entity exists within.
     *
     * @return the world of blocks that this link entity exists within.
     */
    @NotNull ServerWorld getBlockWorld();

    /**
     * Gets the world of graphs that this link entity exists within.
     *
     * @return the world of graphs that this link entity exists within.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the holder for the first node in this link entity's link.
     *
     * @return the holder for the first node in this link entity's link.
     */
    default @NotNull NodeHolder<BlockNode> getFirst() {
        return getHolder().getFirst();
    }

    /**
     * Gets the holder for the second node in this link entity's link.
     *
     * @return the holder for the second node in this link entity's link.
     */
    default @NotNull NodeHolder<BlockNode> getSecond() {
        return getHolder().getSecond();
    }

    /**
     * Gets the block position of the first node in this link entity's link.
     *
     * @return the block position of the first node in this link entity's link.
     */
    default @NotNull BlockPos getFirstBlockPos() {
        return getHolder().getFirst().getPos();
    }

    /**
     * Gets the block position of the second node in the link entity's link.
     *
     * @return the block position of the second node in the link entity's link.
     */
    default @NotNull BlockPos getSecondBlockPos() {
        return getHolder().getSecond().getPos();
    }

    /**
     * Converts this link entity context into a link context, for use in calling link key methods.
     *
     * @return this link entity context as a link context.
     */
    default @NotNull LinkContext toLinkContext() {
        return new LinkContext(getHolder(), getBlockWorld(), getGraphWorld());
    }
}
