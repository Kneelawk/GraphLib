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

    /**
     * Encodes this NodePos to an NBT compound.
     * <p>
     * This writes to the {@code x}, {@code y}, {@code z}, {@code type}, and {@code node} elements.
     *
     * @param nbt the NBT compound to write to.
     */
    public void toNbt(@NotNull CompoundTag nbt) {
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putString("type", node.getType().getId().toString());
        Tag nodeNbt = node.toTag();
        if (nodeNbt != null) {
            nbt.put("node", nodeNbt);
        }
    }

    /**
     * Encodes this NodePos to an NBT compound.
     *
     * @return the encoded NBT compound.
     */
    public @NotNull CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        toNbt(nbt);
        return nbt;
    }

    /**
     * Decodes a NodePos from an NBT compound.
     *
     * @param nbt      the NBT compound to decode from.
     * @param universe the universe that the block node's decoder is to be retrieved from.
     * @return a newly decoded NodePos.
     */
    public static @Nullable NodePos fromNbt(@NotNull CompoundTag nbt, @NotNull GraphUniverse universe) {
        BlockPos pos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));

        ResourceLocation typeId = new ResourceLocation(nbt.getString("type"));
        BlockNodeType type = universe.getNodeType(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown block node type id {} in universe {}", typeId, universe.getId());
            return null;
        }

        BlockNode node = type.getCodec().decode(nbt.get("node"));
        if (node == null) {
            GLLog.warn("Failed to decode block node {}", type.getId());
            return null;
        }

        return new NodePos(pos, node);
    }
}
