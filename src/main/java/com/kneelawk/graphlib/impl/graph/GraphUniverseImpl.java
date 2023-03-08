package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world);

    GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites);
}
