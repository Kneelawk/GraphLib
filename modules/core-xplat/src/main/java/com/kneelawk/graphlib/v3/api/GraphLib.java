package com.kneelawk.graphlib.v3.api;

import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.v3.api.graph.GraphUniverse;
import com.kneelawk.graphlib.v3.impl.GraphLibImpl;

/**
 * GraphLib Public API.
 */
public final class GraphLib {
    private GraphLib() {}

    /**
     * Gets whether the given universe has been registered.
     *
     * @param universeId the id of the universe to check.
     * @return {@code true} if the universe has been registered.
     */
    public static boolean universeExists(ResourceLocation universeId) {
        return GraphLibImpl.UNIVERSES.containsKey(universeId);
    }

    /**
     * Gets the graph universe registered with the given id if any.
     *
     * @param universeId the id of the graph universe to lookup.
     * @return the graph universe with the given id or {@code null} if none could be found.
     */
    public static @Nullable GraphUniverse getUniverse(ResourceLocation universeId) {
        return GraphLibImpl.UNIVERSES.get(universeId);
    }

    /**
     * Gets the graph universe given with the given id or throws.
     *
     * @param universeId the id of the graph universe to lookup.
     * @return the graph universe with the given id.
     * @throws NoSuchElementException if no graph universe could be found with the given id.
     */
    public static GraphUniverse getUniverseOrThrow(ResourceLocation universeId) {
        GraphUniverse universe = getUniverse(universeId);
        if (universe == null) throw new NoSuchElementException("No universe exists with the name " + universeId);
        return universe;
    }
}
