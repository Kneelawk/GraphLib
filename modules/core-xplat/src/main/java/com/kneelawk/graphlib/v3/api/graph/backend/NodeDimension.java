package com.kneelawk.graphlib.v3.api.graph.backend;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

/**
 * Handles nodes for a single node dimension.
 */
public interface NodeDimension {
    /**
     * {@return the level associated with this node dimension, if this dimension is actually associated with a concrete level}
     */
    @Nullable Level getLevel();
}
