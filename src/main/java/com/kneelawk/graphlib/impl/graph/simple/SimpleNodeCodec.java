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
import com.kneelawk.graphlib.api.node.BlockNodeFactory;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public record SimpleNodeCodec(@NotNull BlockPos pos, @NotNull BlockNodeFactory factory) {
    public SimpleNodeCodec(@NotNull BlockPos pos, @NotNull BlockNodeFactory factory) {
        this.pos = pos.toImmutable();
        this.factory = factory;
    }

    public static @NotNull NbtCompound encode(@NotNull BlockPos pos, @NotNull BlockNode node) {
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
    public static SimpleNodeCodec decode(GraphUniverseImpl universe, @NotNull NbtCompound tag) {
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));

        Identifier typeId = new Identifier(tag.getString("type"));
        BlockNodeDecoder decoder = universe.getNodeDecoder(typeId);

        if (decoder == null) {
            GLLog.warn("Tried to load unknown BlockNode type: {} @ {}", typeId, pos);
            return null;
        }

        NbtElement nodeTag = tag.get("node");
        BlockNodeFactory factory = ctx -> {
            BlockNode node = decoder.decode(nodeTag, ctx);

            if (node == null) {
                GLLog.warn("Unable to decode BlockNode with type: {} @ {}", typeId, pos);
            }

            return node;
        };

        return new SimpleNodeCodec(pos, factory);
    }
}
