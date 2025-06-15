package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.Level;

import com.kneelawk.graphlib.v3.api.pos.DimensionRef;

/**
 * An immutable view of all graphs in a save (across all levels).
 */
public interface GraphView {
    /**
     * {@return whether this graph view is on the logical client}
     */
    default boolean isClient() {
        return true;
    }

    /**
     * {@return the graph universe associated with this graph view}
     */
    GraphUniverse getUniverse();
}
