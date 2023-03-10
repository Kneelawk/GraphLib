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
     * @param universeId the unique id of the GraphUniverse to be built.
     * @return a new builder for building a GraphUniverse.
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Builder builder(@NotNull Identifier universeId) {
        return new SimpleGraphUniverseBuilder(universeId);
    }

    /**
     * A builder for {@link GraphUniverse}s.
     */
    interface Builder {
        /**
         * Gets the unique id of the universe this is building.
         *
         * @return this builder's universe's unique id.
         */
        @NotNull Identifier getId();

        /**
         * Builds and registers the {@link GraphUniverse} described by this builder.
         * <p>
         * Right before each GraphUniverse is built, GraphLib invokes the {@code graphlib:universe_build} entrypoint for all
         * mods, to allow for cross-mod compatibility.
         *
         * @return the newly created {@link GraphUniverse}.
         */
        @NotNull GraphUniverse build();
    }
}
