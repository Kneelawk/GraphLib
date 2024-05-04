package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.impl.GLLog;

/**
 * Represents a positioned block node.
 *
 * @param pos  the block pos.
 * @param node the block node.
 */
public record NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
    /**
     * Gets a node pos codec for node poses in the given universe.
     *
     * @param universe the universe to find nodes in.
     * @return a node pos codec for node poses in the given universe.
     */
    public static Codec<NodePos> codec(GraphUniverse universe) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(pos -> pos.pos.getX()),
            Codec.INT.fieldOf("y").forGetter(pos -> pos.pos.getY()),
            Codec.INT.fieldOf("z").forGetter(pos -> pos.pos.getZ()),
            BlockNode.mapCodec(universe).forGetter(pos -> pos.node)
        ).apply(instance, (x, y, z, node) -> new NodePos(new BlockPos(x, y, z), node)));
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
}
