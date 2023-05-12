package com.kneelawk.graphlib.impl.graph.simple;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.GraphWorldImpl;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

public class SimpleGraphUniverse implements GraphUniverse, GraphUniverseImpl {
    private final Identifier id;
    private final List<BlockNodeDiscoverer> discoverers;
    private final Map<Identifier, BlockNodeDecoder> decoders;

    public SimpleGraphUniverse(Identifier universeId, SimpleGraphUniverseBuilder builder) {
        this.id = universeId;
        this.discoverers = new ArrayList<>(builder.discoverers);
        this.decoders = new LinkedHashMap<>(builder.decoders);
    }

    @Override
    public @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world) {
        return StorageHelper.getStorage(world).get(id);
    }

    @Override
    public @NotNull Identifier getId() {
        return id;
    }

    @Override
    public GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites) {
        return new SimpleGraphWorld(this, world, path, syncChunkWrites);
    }

    @Override
    public @NotNull Set<BlockNode> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos) {
        return discoverers.stream()
            .flatMap(discoverer -> discoverer.getNodesInBlock(world, pos).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public @Nullable BlockNodeDecoder getDecoder(@NotNull Identifier typeId) {
        return decoders.get(typeId);
    }
}
