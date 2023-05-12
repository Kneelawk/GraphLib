package com.kneelawk.graphlib.api.wire;

/**
 * The type of connection a sided wire block node could be forming.
 */
public enum WireConnectionType {
    /**
     * Inside corner connection to a wire within the same block.
     */
    INTERNAL,

    /**
     * Flat connection to a wire in an adjacent block.
     */
    EXTERNAL,

    /**
     * Outside corner connection to a wire on an adjacent side of the block this wire is sitting on.
     */
    CORNER,

    /**
     * Only currently used for connecting to full-block nodes under the wire.
     */
    UNDER,

    /**
     * Connection to something above this wire but in the same block, like a center-wire (e.g. standing cables, lamps,
     * powerline-connectors, etc.).
     */
    ABOVE
}
