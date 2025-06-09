package com.kneelawk.graphlib.v3.api.pos;

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
