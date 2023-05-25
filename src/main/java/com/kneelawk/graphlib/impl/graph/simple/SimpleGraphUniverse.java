package com.kneelawk.graphlib.impl.graph.simple;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.api.node.NodeEntityDecoder;
import com.kneelawk.graphlib.api.util.ColorUtils;
import com.kneelawk.graphlib.api.world.SaveMode;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.GraphWorldImpl;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

public class SimpleGraphUniverse implements GraphUniverse, GraphUniverseImpl {
    private final Identifier id;
    private final List<BlockNodeDiscoverer> discoverers = new ArrayList<>();
    private final Map<Identifier, BlockNodeDecoder> nodeDecoders = new LinkedHashMap<>();
    private final Object2IntMap<Identifier> typeIndices = new Object2IntLinkedOpenHashMap<>();
    private final Map<Identifier, NodeEntityDecoder> nodeEntityDecoders = new LinkedHashMap<>();
    final SaveMode saveMode;

    public SimpleGraphUniverse(Identifier universeId, SimpleGraphUniverseBuilder builder) {
        this.id = universeId;
        saveMode = builder.saveMode;
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
    public @NotNull SaveMode getSaveMode() {
        return saveMode;
    }

    @Override
    public void addDiscoverer(@NotNull BlockNodeDiscoverer discoverer) {
        discoverers.add(discoverer);
    }

    @Override
    public void addDiscoverers(@NotNull BlockNodeDiscoverer... discoverers) {
        this.discoverers.addAll(Arrays.asList(discoverers));
    }

    @Override
    public void addDiscoverers(@NotNull Iterable<BlockNodeDiscoverer> discoverers) {
        for (BlockNodeDiscoverer discoverer : discoverers) {
            this.discoverers.add(discoverer);
        }
    }

    @Override
    public void addDiscoverers(@NotNull Collection<BlockNodeDiscoverer> discoverers) {
        this.discoverers.addAll(discoverers);
    }

    @Override
    public void addNodeDecoder(@NotNull Identifier typeId, @NotNull BlockNodeDecoder decoder) {
        nodeDecoders.put(typeId, decoder);
        typeIndices.put(typeId, typeIndices.size());
    }

    @Override
    public void addNodeDecoders(@NotNull Pair<Identifier, ? extends BlockNodeDecoder> @NotNull ... decoders) {
        for (Pair<Identifier, ? extends BlockNodeDecoder> pair : decoders) {
            this.nodeDecoders.put(pair.key(), pair.value());
            typeIndices.put(pair.key(), typeIndices.size());
        }
    }

    @Override
    public void addNodeDecoders(@NotNull Map<Identifier, ? extends BlockNodeDecoder> decoders) {
        this.nodeDecoders.putAll(decoders);
        for (Identifier id : decoders.keySet()) {
            typeIndices.put(id, typeIndices.size());
        }
    }

    @Override
    public void addNodeEntityDecoder(@NotNull Identifier typeId, @NotNull NodeEntityDecoder decoder) {
        this.nodeEntityDecoders.put(typeId, decoder);
    }

    @Override
    public void addNodeEntityDecoders(@NotNull Pair<Identifier, ? extends NodeEntityDecoder>... decoders) {
        for (Pair<Identifier, ? extends NodeEntityDecoder> pair : decoders) {
            this.nodeEntityDecoders.put(pair.key(), pair.value());
        }
    }

    @Override
    public void addNodeEntityDecoders(@NotNull Map<Identifier, ? extends NodeEntityDecoder> decoders) {
        this.nodeEntityDecoders.putAll(decoders);
    }

    @Override
    public void register() {
        GraphLibImpl.register(this);
    }

    @Override
    public int getDefaultDebugColor(@NotNull Identifier typeId) {
        return ColorUtils.hsba2Argb((float) typeIndices.getInt(typeId) / (float) typeIndices.size(), 1f, 1f, 1f);
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
    public @Nullable BlockNodeDecoder getNodeDecoder(@NotNull Identifier typeId) {
        return nodeDecoders.get(typeId);
    }

    @Override
    public @Nullable NodeEntityDecoder getNodeEntityDecoder(@NotNull Identifier typeId) {
        return nodeEntityDecoders.get(typeId);
    }
}