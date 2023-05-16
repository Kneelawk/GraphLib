package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public record SimplePositionedNode(@NotNull BlockPos pos, @NotNull BlockNode node) {
    public SimplePositionedNode(@NotNull BlockPos pos, @NotNull BlockNode node) {
        this.pos = pos.toImmutable();
        this.node = node;
    }

    public @NotNull NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());

        NbtElement nodeTag = node.toTag();
        if (nodeTag != null) {
            tag.put("node", nodeTag);
        }

        tag.putString("type", node.getTypeId().toString());

        return tag;
    }

    @Nullable
    public static SimplePositionedNode fromTag(@NotNull GraphUniverseImpl universe, @NotNull NbtCompound tag,
                                               long graphId) {
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));

        Identifier typeId = new Identifier(tag.getString("type"));
        BlockNodeDecoder decoder = universe.getDecoder(typeId);

        if (decoder == null) {
            GLLog.warn("Tried to load unknown BlockNode type: {} @ {}", typeId, pos);
            return null;
        }

        NbtElement nodeTag = tag.get("node");
        BlockNode node = decoder.createBlockNodeFromTag(nodeTag);

        if (node == null) {
            GLLog.warn("Unable to decode BlockNode with type: {} @ {}", typeId, pos);
            return null;
        }

        return new SimplePositionedNode(pos, node, graphId);
    }
}
