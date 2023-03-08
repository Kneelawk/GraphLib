package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    final Identifier universeId;

    public SimpleGraphUniverseBuilder(Identifier universeId) {
        this.universeId = universeId;
    }

    @Override
    public @NotNull GraphUniverse build() {
        SimpleGraphUniverse universe = new SimpleGraphUniverse(this);
        GraphLibImpl.register(universe);
        return universe;
    }
}
