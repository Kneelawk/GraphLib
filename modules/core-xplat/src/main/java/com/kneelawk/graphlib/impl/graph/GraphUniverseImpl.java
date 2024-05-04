package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.impl.graph.listener.UniverseListener;

public interface GraphUniverseImpl extends GraphUniverse {
    @Override
    @NotNull
    ServerGraphWorldImpl getGraphWorld(@NotNull ServerLevel world);

    ServerGraphWorldImpl createGraphWorld(LevelStorageSource.LevelStorageAccess session, ServerLevel world, Path path,
                                          boolean syncChunkWrites);

    void addListener(ResourceLocation key, UniverseListener listener);

    @NotNull
    Set<BlockNode> discoverNodesInBlock(@NotNull ServerLevel world, @NotNull BlockPos pos);
}
