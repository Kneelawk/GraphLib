package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntityDecoder;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world);

    GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites);

    @NotNull Set<BlockNode> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);

    @Nullable BlockNodeDecoder getNodeDecoder(@NotNull Identifier typeId);

    @Nullable NodeEntityDecoder getNodeEntityDecoder(@NotNull Identifier typeId);

    @Nullable LinkKeyDecoder getLinkKeyDecoder(@NotNull Identifier typeId);

    @Nullable LinkEntityDecoder getLinkEntityDecoder(@NotNull Identifier typeId);

    @Nullable GraphEntityType<?> getGraphEntityType(@NotNull Identifier typeId);

    @NotNull Iterable<GraphEntityType<?>> getAllGraphEntityTypes();
}
