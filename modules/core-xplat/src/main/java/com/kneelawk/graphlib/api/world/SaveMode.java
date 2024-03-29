package com.kneelawk.graphlib.api.world;

/**
 * Describes when an object should be saved.
 */
public enum SaveMode {
    /**
     * Only save things when their associated world chunk is saved or unloaded.
     * <p>
     * This is best for graphs that are updated frequently, and where minor graph corruption is not a big deal.
     */
    UNLOAD,
    /**
     * Save some things every tick.
     * <p>
     * This is best for graphs that are updated less-frequently, and where graph corruption is to be avoided.
     */
    INCREMENTAL,
    /**
     * Save <b>all</b> unsaved things every tick.
     * <p>
     * This is best for graphs that are updated very infrequently, but that must really avoid graph corruption.
     */
    IMMEDIATE
}
