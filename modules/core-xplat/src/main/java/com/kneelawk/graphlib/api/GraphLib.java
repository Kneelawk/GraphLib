package com.kneelawk.graphlib.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, like obtaining a
 * previously-registered {@link GraphUniverse}.
 */
public final class GraphLib {
    private GraphLib() {
    }

    /**
     * A registry key for the universe registry.
     * <p>
     * Use this key on NeoForge to either create a DeferredRegistry or to listen for the event for when to register.
     */
    @SuppressWarnings("unchecked")
    public static final RegistryKey<Registry<GraphUniverse>> UNIVERSE_KEY =
        (RegistryKey<Registry<GraphUniverse>>) (RegistryKey<?>) GraphLibImpl.UNIVERSE_KEY;

    /**
     * The unique id of the universe representing the data managed by pre-1.0 versions of GraphLib.
     *
     * @deprecated It is recommended for mods to build and use their own universes with {@link GraphUniverse#builder()}.
     */
    @Deprecated
    public static final Identifier LEGACY_UNIVERSE_ID = Constants.id(Constants.GRAPHDATA_DIRNAME);

    /**
     * The universe representing the data managed by pre-1.0 versions of GraphLib.
     *
     * @deprecated It is recommended for mods to build and use their own universes with {@link GraphUniverse#builder()}.
     */
    @Deprecated
    public static final GraphUniverse LEGACY_UNIVERSE = GraphUniverse.builder().build(LEGACY_UNIVERSE_ID);

    /**
     * Gets whether the given universe has been registered.
     *
     * @param universeId the id of the universe to check.
     * @return {@code true} if the universe has been registered.
     */
    public static boolean universeExists(@NotNull Identifier universeId) {
        return GraphLibPlatform.INSTANCE.getUniverseRegistry().containsId(universeId);
    }

    /**
     * Gets a registered {@link GraphUniverse} by its id.
     *
     * @param universeId the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static @NotNull GraphUniverse getUniverse(@NotNull Identifier universeId) {
        GraphUniverseImpl graphUniverse = GraphLibPlatform.INSTANCE.getUniverseRegistry().get(universeId);
        if (graphUniverse == null) {
            throw new IllegalArgumentException("No universe exists with the name " + universeId);
        }

        return graphUniverse;
    }
}
