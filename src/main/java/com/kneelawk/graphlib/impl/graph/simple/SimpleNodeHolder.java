package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.v1.graph.NodeHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class SimpleNodeHolder implements NodeHolder {
    private final @NotNull BlockPos pos;
    private final @NotNull BlockNode node;

    long graphId;

    public SimpleNodeHolder(@NotNull BlockPos pos, @NotNull BlockNode node, long graphId) {
        this.pos = pos.toImmutable();
        this.node = node;
        this.graphId = graphId;
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
    public static SimpleNodeHolder fromTag(@NotNull GraphUniverseImpl universe, @NotNull NbtCompound tag,
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

        return new SimpleNodeHolder(pos, node, graphId);
    }

    @Override
    public @NotNull BlockPos getPos() {
        return pos;
    }

    @Override
    public @NotNull BlockNode getNode() {
        return node;
    }

    @Override
    public long getGraphId() {
        return graphId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleNodeHolder) obj;
        return Objects.equals(this.pos, that.pos) && Objects.equals(this.node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, node);
    }

    @Override
    public String toString() {
        return "BlockNodeWrapper[" + "pos=" + pos + ", " + "graphId=" + graphId + ", " + "node=" + node + ']';
    }
}
