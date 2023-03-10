package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    @Override
    public @NotNull GraphUniverse buildAndRegister(@NotNull Identifier universeId) {
        GraphLibImpl.preBuild(universeId, this);

        SimpleGraphUniverse universe = new SimpleGraphUniverse(universeId, this);
        GraphLibImpl.register(universe);
        return universe;
    }
}
