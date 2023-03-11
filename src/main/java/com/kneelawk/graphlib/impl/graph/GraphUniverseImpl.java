package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world);

    GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites);

    @NotNull Set<BlockNode> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);

    @Nullable BlockNodeDecoder getDecoder(@NotNull Identifier typeId);
}
