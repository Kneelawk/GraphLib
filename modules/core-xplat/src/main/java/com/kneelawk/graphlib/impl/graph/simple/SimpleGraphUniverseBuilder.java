package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.world.SaveMode;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    SaveMode saveMode = SaveMode.UNLOAD;

    @Override
    public @NotNull GraphUniverse build(@NotNull ResourceLocation universeId) {
        return new SimpleGraphUniverse(universeId, this);
    }

    @Override
    public GraphUniverse.@NotNull Builder saveMode(@NotNull SaveMode saveMode) {
        this.saveMode = saveMode;
        return this;
    }
}
