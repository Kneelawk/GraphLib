package com.kneelawk.graphlib.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, like obtaining a
 * previously-registered {@link GraphUniverse}.
 */
public final class GraphLib {
    private GraphLib() {
    }

    /**
     * Gets whether the given universe has been registered.
     *
     * @param universeId the id of the universe to check.
     * @return {@code true} if the universe has been registered.
     */
    public static boolean universeExists(@NotNull ResourceLocation universeId) {
        return GraphLibImpl.UNIVERSE.containsKey(universeId);
    }

    /**
     * Gets a registered {@link GraphUniverse} by its id.
     *
     * @param universeId the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static @NotNull GraphUniverse getUniverse(@NotNull ResourceLocation universeId) {
        GraphUniverseImpl graphUniverse = GraphLibImpl.UNIVERSE.get(universeId);
        if (graphUniverse == null) {
            throw new IllegalArgumentException("No universe exists with the name " + universeId);
        }

        return graphUniverse;
    }
}
