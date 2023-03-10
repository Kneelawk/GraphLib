package com.kneelawk.graphlib.api.v1.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphUniverseBuilder;

/**
 * Represents one {@link GraphWorld} per {@link ServerWorld}. Provides access to each world's associated {@link GraphWorld}.
 */
public interface GraphUniverse {
    /**
     * Gets the {@link GraphWorld} for the given {@link ServerWorld}.
     *
     * @param world the world whose BlockGraphController is to be obtained.
     * @return the GraphWorld of the given world.
     */
    @NotNull GraphWorld getGraphWorld(@NotNull ServerWorld world);

    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    @NotNull Identifier getId();

    /**
     * Creates a new GraphUniverse builder.
     *
     * @return a new builder for building a GraphUniverse.
     */
    @Contract(value = "-> new", pure = true)
    static @NotNull Builder builder() {
        return new SimpleGraphUniverseBuilder();
    }

    /**
     * A builder for {@link GraphUniverse}s.
     */
    interface Builder {
        /**
         * Builds and registers the {@link GraphUniverse} described by this builder.
         * <p>
         * Right before each GraphUniverse is built, GraphLib invokes the {@code graphlib:universe_build} entrypoint for all
         * mods, to allow for cross-mod compatibility.
         *
         * @param universeId the unique id of the GraphUniverse to be built.
         * @return the newly created {@link GraphUniverse}.
         */
        @NotNull GraphUniverse build(@NotNull Identifier universeId);
    }
}
