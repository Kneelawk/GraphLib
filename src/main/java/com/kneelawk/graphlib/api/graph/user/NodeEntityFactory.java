package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.NodeEntityContext;

/**
 * Used for creating a new node entity.
 */
public interface NodeEntityFactory {
    /**
     * Creates a new node entity.
     *
     * @param ctx the node entity context for the new node entity.
     * @return a new node entity, if one can be created.
     */
    @Nullable NodeEntity createNew(@NotNull NodeEntityContext ctx);
}
