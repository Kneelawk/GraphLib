package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.LinkEntityContext;

/**
 * Used for creating a new link entity.
 */
@FunctionalInterface
public interface LinkEntityFactory {
    /**
     * Creates a new link entity.
     *
     * @param ctx the link entity context for the new link entity.
     * @return a new link entity, if one can be created.
     */
    @Nullable LinkEntity createNew(@NotNull LinkEntityContext ctx);
}
