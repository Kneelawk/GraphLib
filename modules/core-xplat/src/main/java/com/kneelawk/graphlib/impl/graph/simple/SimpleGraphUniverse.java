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

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDiscoverer;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.ColorUtils;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.world.SaveMode;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.listener.UniverseListener;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

public class SimpleGraphUniverse implements GraphUniverse, GraphUniverseImpl {
    private final Identifier id;
    private final List<BlockNodeDiscoverer> discoverers = new ArrayList<>();
    private final Map<Identifier, BlockNodeType> nodeTypes = new LinkedHashMap<>();
    private final Object2IntMap<Identifier> typeIndices = new Object2IntLinkedOpenHashMap<>();
    private final Map<Identifier, NodeEntityType> nodeEntityTypes = new LinkedHashMap<>();
    private final Map<Identifier, LinkKeyType> linkKeyTypes = new LinkedHashMap<>();
    private final Map<Identifier, LinkEntityType> linkEntityTypes = new LinkedHashMap<>();
    private final Map<Identifier, GraphEntityType<?>> graphEntityTypes = new LinkedHashMap<>();
    private final Set<CacheCategory<?>> cacheCategories = new ObjectLinkedOpenHashSet<>();
    final Map<Identifier, UniverseListener> listeners = new LinkedHashMap<>();
    final SaveMode saveMode;

    public SimpleGraphUniverse(Identifier universeId, SimpleGraphUniverseBuilder builder) {
        this.id = universeId;
        saveMode = builder.saveMode;

        addLinkKeyType(EmptyLinkKey.TYPE);
    }

    @Override
    public @NotNull ServerGraphWorldImpl getServerGraphWorld(@NotNull ServerWorld world) {
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
    public void addNodeType(@NotNull BlockNodeType type) {
        nodeTypes.put(type.getId(), type);
        typeIndices.put(type.getId(), typeIndices.size());
    }

    @Override
    public void addNodeEntityType(@NotNull NodeEntityType type) {
        this.nodeEntityTypes.put(type.getId(), type);
    }

    @Override
    public void addLinkKeyType(@NotNull LinkKeyType type) {
        this.linkKeyTypes.put(type.getId(), type);
    }

    @Override
    public void addLinkEntityType(@NotNull LinkEntityType type) {
        this.linkEntityTypes.put(type.getId(), type);
    }

    @Override
    public void addGraphEntityType(@NotNull GraphEntityType<?> type) {
        this.graphEntityTypes.put(type.getId(), type);
    }

    @Override
    public void addCacheCategory(@NotNull CacheCategory<?> category) {
        cacheCategories.add(category);
    }

    @Override
    public @NotNull Iterable<CacheCategory<?>> getCacheCatetories() {
        return cacheCategories;
    }

    @Override
    public void register() {
        GraphLibImpl.register(this);
    }

    @Override
    public int getNodeTypeIndex(@NotNull Identifier typeId) {
        return typeIndices.getInt(typeId);
    }

    @Override
    public int getNodeTypeCount() {
        return typeIndices.size();
    }

    @Override
    public ServerGraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites) {
        return new SimpleServerGraphWorld(this, world, path, syncChunkWrites);
    }

    @Override
    public void addListener(Identifier key, UniverseListener listener) {
        listeners.put(key, listener);
    }

    @Override
    public @NotNull Set<BlockNode> discoverNodesInBlock(@NotNull ServerWorld world, @NotNull BlockPos pos) {
        return discoverers.stream()
            .flatMap(discoverer -> discoverer.getNodesInBlock(world, pos).stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public @Nullable BlockNodeType getNodeType(@NotNull Identifier typeId) {
        return nodeTypes.get(typeId);
    }

    @Override
    public @Nullable NodeEntityType getNodeEntityType(@NotNull Identifier typeId) {
        return nodeEntityTypes.get(typeId);
    }

    @Override
    public @Nullable LinkKeyType getLinkKeyType(@NotNull Identifier typeId) {
        return linkKeyTypes.get(typeId);
    }

    @Override
    public @Nullable LinkEntityType getLinkEntityType(@NotNull Identifier typeId) {
        return linkEntityTypes.get(typeId);
    }

    @Override
    public @Nullable GraphEntityType<?> getGraphEntityType(@NotNull Identifier typeId) {
        return graphEntityTypes.get(typeId);
    }

    @Override
    public @NotNull Collection<GraphEntityType<?>> getAllGraphEntityTypes() {
        return graphEntityTypes.values();
    }
}