package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.impl.graph.listener.UniverseListener;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull ServerGraphWorldImpl getServerGraphWorld(@NotNull ServerWorld world);

    ServerGraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites);

    void addListener(Identifier key, UniverseListener listener);

    @NotNull Set<BlockNode> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos);
}
