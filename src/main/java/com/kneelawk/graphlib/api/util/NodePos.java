package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder;

/**
 * Represents a positioned block node.
 *
 * @param pos  the block pos.
 * @param node the block node.
 */
public record NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
    /**
     * Creates a positioned block node representation.
     *
     * @param pos  the block pos.
     * @param node the block node.
     */
    public NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
        this.pos = pos.toImmutable();
        this.node = node;
    }

    /**
     * Encodes this NodePos to an NBT compound.
     * <p>
     * This writes to the {@code x}, {@code y}, {@code z}, {@code type}, and {@code node} elements.
     *
     * @param nbt the NBT compound to write to.
     */
    public void toNbt(@NotNull NbtCompound nbt) {
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putString("type", node.getTypeId().toString());
        NbtElement nodeNbt = node.toTag();
        if (nodeNbt != null) {
            nbt.put("node", nodeNbt);
        }
    }

    /**
     * Encodes this NodePos to an NBT compound.
     *
     * @return the encoded NBT compound.
     */
    public @NotNull NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
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
    public static @Nullable NodePos fromNbt(@NotNull NbtCompound nbt, @NotNull GraphUniverse universe) {
        BlockPos pos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
        Identifier typeId = new Identifier(nbt.getString("type"));
        BlockNodeDecoder decoder = universe.getNodeDecoder(typeId);
        if (decoder == null) return null;
        BlockNode node = decoder.decode(nbt.get("node"));
        if (node == null) return null;
        return new NodePos(pos, node);
    }
}
