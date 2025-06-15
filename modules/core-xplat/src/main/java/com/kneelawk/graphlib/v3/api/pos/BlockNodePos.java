package com.kneelawk.graphlib.v3.api.pos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

import com.kneelawk.graphlib.v3.api.graph.user.GraphNode;

import static com.kneelawk.graphlib.v3.impl.GLConstants.rl;

/**
 * Represents a node positioned in a specific block.
 *
 * @param blockPos  the block the node is in.
 * @param dimension the dimension the node is in.
 * @param node      the node itself.
 */
public record BlockNodePos(BlockPos blockPos, DimensionRef dimension, GraphNode node) implements NodePos {
    /**
     * {@link BlockNodePos} codec.
     */
    public static final MapCodec<BlockNodePos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(BlockNodePos::blockPos),
        DimensionRef.MAP_CODEC.codec().fieldOf("dim").forGetter(BlockNodePos::dimension),
        GraphNode.MAP_CODEC.codec().fieldOf("node").forGetter(BlockNodePos::node)
    ).apply(instance, BlockNodePos::new));
    /**
     * {@link BlockNodePos} type.
     */
    public static final Type<BlockNodePos> TYPE = new Type<>(rl("block"), MAP_CODEC);

    @Override
    public Type<? extends NodePos> getType() {
        return TYPE;
    }

    @Override
    public boolean isStaticDimension() {
        return true;
    }

    @Override
    public boolean isStaticBlockPos() {
        return true;
    }
}
