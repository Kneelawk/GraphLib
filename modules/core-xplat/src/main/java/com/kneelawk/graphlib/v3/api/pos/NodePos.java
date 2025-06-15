package com.kneelawk.graphlib.v3.api.pos;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.v3.api.graph.user.GraphNode;

/**
 * Generic Node position. This can have different implementations, depending on what the node is actually attached to.
 */
public interface NodePos {
    /**
     * Utility method for creating a block node position.
     *
     * @param pos       the block position of the node.
     * @param dimension the dimension the node is in.
     * @param node      the node itself.
     * @return the node pos for uniquely describing that node.
     */
    static NodePos block(BlockPos pos, ResourceLocation dimension, GraphNode node) {
        return new BlockNodePos(pos, new LevelDimensionRef(dimension), node);
    }

    /**
     * {@return the graph node of this node pos}
     */
    GraphNode node();

    /**
     * {@return whether this node pos is associated with a single static dimension}
     * <p>
     * This returns {@code true} if and only if what ever this node pos is associated with cannot move to another
     * dimension. Blocks, even if pushed by pistons, cannot be moved between dimensions under normal circumstances.
     * Entities on the other hand, can be "teleported" to another dimension.
     */
    default boolean isStaticDimension() {
        return false;
    }

    /**
     * {@return the dimension that this node pos is associated with, if any}
     */
    default @Nullable DimensionRef dimension() {
        return null;
    }

    /**
     * {@return whether this node pos is associated with a single static block pos}
     * <p>
     * This returns {@code true} if and only if what ever this node pos is associated with cannot move to another
     * block pos.
     */
    default boolean isStaticBlockPos() {
        return false;
    }

    /**
     * {@return the block pos this node pos is associated with, if any}
     */
    default @Nullable BlockPos blockPos() {
        return null;
    }

    /**
     * {@return this dimension ref's type}
     */
    Type<? extends NodePos> getType();

    /**
     * The type of a node position.
     *
     * @param id    the id of this type of node position within a given universe.
     * @param codec the codec for encoding and decoding this node position type.
     * @param <T>   the type of node pos this type describes.
     */
    record Type<T extends NodePos>(ResourceLocation id, MapCodec<T> codec) {}
}
