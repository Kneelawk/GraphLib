package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public final class SimpleNodeWrapper {
    public static final MapCodec<SimpleNodeWrapper> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        NodePos.MAP_CODEC.forGetter(SimpleNodeWrapper::pos),
        SimpleBlockGraph.GRAPH_ID.retrieve()
    ).apply(instance, SimpleNodeWrapper::new));

    final @NotNull NodePos pos;

    long graphId;

    public SimpleNodeWrapper(@NotNull NodePos pos, long graphId) {
        this.pos = pos;
        this.graphId = graphId;
    }

    public @NotNull NodePos pos() {
        return pos;
    }

    public @NotNull BlockPos blockPos() {
        return pos.pos();
    }

    public @NotNull BlockNode node() {
        return pos.node();
    }

    public long getGraphId() {
        return graphId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleNodeWrapper that = (SimpleNodeWrapper) o;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleNodeWrapper[" + pos + " in graph " + graphId + ']';
    }
}
