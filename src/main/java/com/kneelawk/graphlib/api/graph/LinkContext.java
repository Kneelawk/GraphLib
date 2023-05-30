package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Context passed to a link key in method calls.
 *
 * @param self       the link's holder.
 * @param blockWorld the world of blocks.
 * @param graphWorld the world of graphs.
 */
public record LinkContext(@NotNull LinkHolder<LinkKey> self, @NotNull ServerWorld blockWorld,
                          @NotNull GraphView graphWorld) {
    /**
     * This constructor is considered internal to GraphLib and not API.
     * <p>
     * This constructor is internal so that fields can be added to link-context without breaking API.
     *
     * @param self       the link's holder.
     * @param blockWorld the world of blocks.
     * @param graphWorld the world of graphs.
     */
    @ApiStatus.Internal
    public LinkContext {
    }

    /**
     * Gets the node holder of the first node in this link.
     *
     * @return the holder of the first node in this link.
     */
    public @NotNull NodeHolder<BlockNode> getFirst() {
        return self.getFirst();
    }

    /**
     * Gets the node holder of the second node in this link.
     *
     * @return the holder of the second node in this link.
     */
    public @NotNull NodeHolder<BlockNode> getSecond() {
        return self.getSecond();
    }

    /**
     * Gets the block position of the first node in this link.
     *
     * @return the block position of the first node in this link.
     */
    public @NotNull BlockPos getFirstBlockPos() {
        return self.getFirst().getPos();
    }

    /**
     * Gets the block position of the second node in this link.
     *
     * @return the block position of the second node in this link.
     */
    public @NotNull BlockPos getSecondBlockPos() {
        return self.getSecond().getPos();
    }

    /**
     * Gets the link entity associated with this link.
     *
     * @return the link entity associated with this link.
     */
    public @Nullable LinkEntity getLinkEntity() {
        BlockGraph graph = graphWorld().getGraph(self.getFirst().getGraphId());
        if (graph != null) {
            return graph.getLinkEntity(self.toLinkPos());
        }
        return null;
    }

    /**
     * Gets the link entity associated with this link, if the correct type.
     *
     * @param entityClass the class of the entity to get.
     * @param <T>         the type of the entity to get.
     * @return the link entity associated with this link, if the correct type.
     */
    public <T extends LinkEntity> @Nullable T getLinkEntity(Class<T> entityClass) {
        LinkEntity entity = getLinkEntity();
        if (entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }
}
