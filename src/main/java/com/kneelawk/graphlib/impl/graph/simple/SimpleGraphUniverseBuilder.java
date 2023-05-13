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

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    List<BlockNodeDiscoverer> discoverers = new ArrayList<>();
    Map<Identifier, BlockNodeDecoder> decoders = new LinkedHashMap<>();

    @Override
    public @NotNull GraphUniverse build(@NotNull Identifier universeId) {
        GraphLibImpl.preBuild(universeId, this);
        return new SimpleGraphUniverse(universeId, this);
    }
}
