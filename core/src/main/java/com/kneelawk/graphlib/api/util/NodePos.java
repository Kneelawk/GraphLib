package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodePacketDecoder;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.net.GLNet;

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
        nbt.putString("type", node.getType().getId().toString());
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
        BlockNodeType type = universe.getNodeType(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown block node type id {} in universe {}", typeId, universe.getId());
            return null;
        }

        BlockNode node = type.getDecoder().decode(nbt.get("node"));
        if (node == null) {
            GLLog.warn("Failed to decode block node {}", type.getId());
            return null;
        }

        return new NodePos(pos, node);
    }
}
