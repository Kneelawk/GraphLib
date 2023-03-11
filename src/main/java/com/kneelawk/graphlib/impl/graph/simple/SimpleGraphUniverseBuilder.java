package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.Pair;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    List<BlockNodeDiscoverer> discoverers = new ArrayList<>();
    Map<Identifier, BlockNodeDecoder> decoders = new LinkedHashMap<>();

    @Override
    public @NotNull GraphUniverse buildAndRegister(@NotNull Identifier universeId) {
        GraphLibImpl.preBuild(universeId, this);

        SimpleGraphUniverse universe = new SimpleGraphUniverse(universeId, this);
        GraphLibImpl.register(universe);
        return universe;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverer(@NotNull BlockNodeDiscoverer discoverer) {
        discoverers.add(discoverer);
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(@NotNull BlockNodeDiscoverer... discoverers) {
        this.discoverers.addAll(Arrays.asList(discoverers));
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(@NotNull Iterable<BlockNodeDiscoverer> discoverers) {
        for (BlockNodeDiscoverer discoverer : discoverers) {
            this.discoverers.add(discoverer);
        }
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(@NotNull Collection<BlockNodeDiscoverer> discoverers) {
        this.discoverers.addAll(discoverers);
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder decoder(@NotNull Identifier typeId, @NotNull BlockNodeDecoder decoder) {
        decoders.put(typeId, decoder);
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder decoders(@NotNull Pair<Identifier, BlockNodeDecoder>... decoders) {
        for (Pair<Identifier, BlockNodeDecoder> pair : decoders) {
            this.decoders.put(pair.key(), pair.value());
        }
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder decoders(@NotNull Map<Identifier, BlockNodeDecoder> decoders) {
        this.decoders.putAll(decoders);
        return this;
    }
}
