package com.kneelawk.graphlib.api.event;

import com.kneelawk.graphlib.api.graph.UniverseModifierRegistry;

/**
 * Entrypoint {@code graphlib:universe_modify} for registering {@link UniverseModifierRegistry}s before any universes are built.
 * <p>
 * Mods looking to modify other mods' universes before they are built should use this entrypoint.
 */
public interface UniverseModifyInitializer {
    /**
     * Called to register any modifiers a mod would want to add to other mods' universes.
     *
     * @param registry the registry to register universe modifiers to.
     */
    void register(UniverseModifierRegistry registry);
}
