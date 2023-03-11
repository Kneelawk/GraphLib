package com.kneelawk.graphlib.impl.graph.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    ArrayList<BlockNodeDiscoverer> blockNodeDiscoverers = new ArrayList<>();

    @Override
    public @NotNull GraphUniverse buildAndRegister(@NotNull Identifier universeId) {
        GraphLibImpl.preBuild(universeId, this);

        SimpleGraphUniverse universe = new SimpleGraphUniverse(universeId, this);
        GraphLibImpl.register(universe);
        return universe;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverer(BlockNodeDiscoverer discoverer) {
        blockNodeDiscoverers.add(discoverer);
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(BlockNodeDiscoverer... discoverers) {
        blockNodeDiscoverers.addAll(Arrays.asList(discoverers));
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(Iterable<BlockNodeDiscoverer> discoverers) {
        for (BlockNodeDiscoverer discoverer : discoverers) {
            blockNodeDiscoverers.add(discoverer);
        }
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder discoverers(Collection<BlockNodeDiscoverer> discoverers) {
        blockNodeDiscoverers.addAll(discoverers);
        return this;
    }
}
