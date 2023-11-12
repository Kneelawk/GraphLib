package com.kneelawk.graphlib.api.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.event.UniverseModifyInitializer;

/**
 * Used for modifying a universe as it is building.
 *
 * @see UniverseModifyInitializer
 */
public interface UniverseModifierRegistry {
    /**
     * Registers a modifier to modify the given universe when it is built.
     *
     * @param universeId the unique id of  the universe to modify.
     * @param modifier   the callback for modifying the given universe when it is built.
     */
    void modify(@NotNull Identifier universeId, @NotNull Modify modifier);

    /**
     * Registers a modifier to modify each universe when it is built.
     *
     * @param modifier the callback for modifying each universe when it is built.
     */
    void modifyAll(@NotNull ModifyAll modifier);

    /**
     * Used for modifying a single universe.
     */
    interface Modify {
        /**
         * Modify a single universe's builder.
         *
         * @param builder the builder for the universe being modified.
         */
        void modify(@NotNull GraphUniverse.Builder builder);
    }

    /**
     * Used for modifying all universes.
     */
    interface ModifyAll {
        /**
         * Modify each universe's builder before it is built.
         *
         * @param universeId the unique id of the universe being built.
         * @param builder    the builder for the universe being modified.
         */
        void modify(@NotNull Identifier universeId, @NotNull GraphUniverse.Builder builder);
    }
}
