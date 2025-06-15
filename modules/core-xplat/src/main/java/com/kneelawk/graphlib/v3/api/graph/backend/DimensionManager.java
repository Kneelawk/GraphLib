package com.kneelawk.graphlib.v3.api.graph.backend;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import com.kneelawk.graphlib.v3.api.pos.DimensionRef;

/**
 * Manages all nodes for a given type of {@link com.kneelawk.graphlib.v3.api.pos.DimensionRef}.
 *
 * @param <R> the type of dimension ref this manager manages.
 */
public interface DimensionManager<R extends DimensionRef> {
    /**
     * Gets the node dimension for the given dimension reference, if the dimension reference actually references a
     * valid node dimension that exists.
     *
     * @param ref the dimension ref that references the given node dimension.
     * @return the node dimension referenced by the given dimension ref.
     */
    @Nullable NodeDimension getDimension(R ref);

    /**
     * Gets the level associated with the given dimension reference if the dimension reference actually references a
     * concrete level.
     *
     * @param ref the dimension reference of the level to lookup.
     * @return the level associated with the given dimension reference.
     */
    default @Nullable Level getLevel(R ref) {
        NodeDimension dimension = getDimension(ref);
        if (dimension == null) return null;
        return dimension.getLevel();
    }
}
