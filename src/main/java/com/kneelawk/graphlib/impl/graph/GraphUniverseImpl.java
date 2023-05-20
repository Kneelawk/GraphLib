package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.node.NodeKeyDecoder;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world);

    GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites);

    @NotNull Map<PosNodeKey, Supplier<BlockNode>> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);

    @Nullable BlockNodeDecoder getNodeDecoder(@NotNull Identifier typeId);

    @Nullable NodeKeyDecoder getNodeKeyDecoder(@NotNull Identifier typeId);
}
