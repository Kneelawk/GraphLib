package com.kneelawk.graphlib.fabric.api;

import net.minecraft.registry.Registry;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.fabric.impl.GraphLibFabricMod;

/**
 * GraphLib public interface for fabric-specific mods.
 */
public class GraphLibFabric {
    private GraphLibFabric() {}

    /**
     * Public graph-universe registry for registering universes.
     */
    @SuppressWarnings("unchecked")
    public static final Registry<GraphUniverse> UNIVERSE =
        (Registry<GraphUniverse>) (Registry<?>) GraphLibFabricMod.UNIVERSE;
}
