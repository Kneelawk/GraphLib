package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.NodeEntity;

/**
 * Context passed to a node in method calls.
 *
 * @param self       this node's holder.
 * @param blockWorld the world of blocks.
 * @param graphWorld the world of graphs.
 */
public record NodeContext(@NotNull NodeHolder<BlockNode> self, @NotNull ServerWorld blockWorld,
                          @NotNull GraphView graphWorld) {
    /**
     * This constructor is considered internal to GraphLib and not API.
     * <p>
     * This constructor is internal so that more fields can be added to node-context without breaking API.
     *
     * @param self       the node's holder.
     * @param blockWorld the world of blocks.
     * @param graphWorld the world of graphs.
     */
    @ApiStatus.Internal
    public NodeContext {
    }

    /**
     * Gets the position of this node context.
     *
     * @return the position of this node context.
     */
    public @NotNull BlockPos getPos() {
        return self.getPos();
    }

    /**
     * Gets the block state of the block at this node's position.
     *
     * @return the block state of the block at this node's position.
     */
    public @NotNull BlockState getBlockState() {
        return blockWorld.getBlockState(getPos());
    }

    /**
     * Gets the block entity at this node's position, if any.
     *
     * @return the block entity at this node's position, if any.
     */
    public @Nullable BlockEntity getBlockEntity() {
        return blockWorld.getBlockEntity(getPos());
    }

    /**
     * Gets the node entity associated with this block node.
     *
     * @return the node entity associated with this block node, or <code>null</code> if this block node's graph does not
     * exist or if this block node has no associated node entity.
     */
    public @Nullable NodeEntity getNodeEntity() {
        BlockGraph graph = graphWorld.getGraph(self.getGraphId());
        if (graph != null) {
            return graph.getNodeEntity(self.toNodePos());
        }
        return null;
    }

    /**
     * Gets the node entity associated with this block node and casts it to the given type, if possible.
     *
     * @param entityClass the class of the node entity to try and get.
     * @param <T>         the type of node entity to try and get.
     * @return the node entity associated with this block node, or <code>null</code> if this block node's graph does not
     * exist, if this block node has no associated node entity, or if this block node's associated node entity is of a
     * different class.
     */
    public <T extends NodeEntity> @Nullable T getNodeEntity(Class<T> entityClass) {
        NodeEntity entity = getNodeEntity();
        if (entityClass.isInstance(entity)) {
            return entityClass.cast(entity);
        }
        return null;
    }
}
