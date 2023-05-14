package com.kneelawk.graphlib.api.world;

/**
 * Describes when an object should be saved.
 */
public enum SaveMode {
    /**
     * Only save things when their associated world chunk is saved or unloaded.
     */
    UNLOAD,
    /**
     * Save some things every tick.
     */
    INCREMENTAL,
    /**
     * Save <b>all</b> unsaved things every tick.
     */
    IMMEDIATE
}
