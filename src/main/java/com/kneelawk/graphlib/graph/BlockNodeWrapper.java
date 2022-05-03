package com.kneelawk.graphlib.graph;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.node.BlockNode;
import com.kneelawk.graphlib.node.BlockNodeDecoder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BlockNodeWrapper<T extends BlockNode>(@NotNull BlockPos pos, @NotNull T node) {
    public NbtCompound toTag() {
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
    public static BlockNodeWrapper<BlockNode> fromTag(NbtCompound tag) {
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));

        Identifier typeId = new Identifier(tag.getString("type"));
        BlockNodeDecoder decoder = GraphLib.BLOCK_NODE_DECODER.get(typeId);

        if (decoder == null) {
            GraphLib.log.warn("Tried to load unknown BlockNode type: {} @ {}", typeId, pos);
            return null;
        }

        NbtElement nodeTag = tag.get("node");
        BlockNode node = decoder.createBlockNodeFromTag(nodeTag);

        if (node == null) {
            GraphLib.log.warn("Unable to decode BlockNode with type: {} @ {}", typeId, pos);
            return null;
        }

        return new BlockNodeWrapper<>(pos, node);
    }
}
