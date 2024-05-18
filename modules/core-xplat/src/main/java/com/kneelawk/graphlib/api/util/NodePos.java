package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Represents a positioned block node.
 *
 * @param pos  the block pos.
 * @param node the block node.
 */
public record NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
    /**
     * Map codec for node poses.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    public static final MapCodec<NodePos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("x").forGetter(pos -> pos.pos.getX()),
        Codec.INT.fieldOf("y").forGetter(pos -> pos.pos.getY()),
        Codec.INT.fieldOf("z").forGetter(pos -> pos.pos.getZ()),
        BlockNode.MAP_CODEC.forGetter(NodePos::node)
    ).apply(instance, (x, y, z, node) -> new NodePos(new BlockPos(x, y, z), node)));

    /**
     * Map codec for node poses that provides its own universe.
     */
    public static final MapCodec<InUniverse<NodePos>> IN_UNIVERSE_MAP_CODEC = InUniverse.mapCodec(MAP_CODEC);

    /**
     * Gets a node pos map codec for node poses in the given universe.
     *
     * @param universe the universe to find nodes in.
     * @return a node pos codec for node poses in the given universe.
     */
    public static MapCodec<NodePos> mapCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingMapCodec(universe, MAP_CODEC);
    }

    /**
     * Creates a positioned block node representation.
     *
     * @param pos  the block pos.
     * @param node the block node.
     */
    public NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
        this.pos = pos.immutable();
        this.node = node;
    }

    @Override
    public String toString() {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + node + ")";
    }
}
