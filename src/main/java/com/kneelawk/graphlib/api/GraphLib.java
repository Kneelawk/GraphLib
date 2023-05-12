package com.kneelawk.graphlib.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

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
    public static final GraphUniverse LEGACY_UNIVERSE = GraphUniverse.builder().buildAndRegister(LEGACY_UNIVERSE_ID);

    /**
     * Gets a registered {@link GraphUniverse} by its id.
     *
     * @param universeId the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static @NotNull GraphUniverse getUniverse(Identifier universeId) {
        GraphUniverseImpl graphUniverse = GraphLibImpl.UNIVERSE.get(universeId);
        if (graphUniverse == null) {
            throw new IllegalArgumentException("No universe exists with the name " + universeId);
        }

        return graphUniverse;
    }
}
