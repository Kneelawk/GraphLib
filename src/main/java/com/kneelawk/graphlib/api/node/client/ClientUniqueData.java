package com.kneelawk.graphlib.api.node.client;

/**
 * Describes unique data that is associated with a {@link ClientBlockNode} that is used as its key.
 */
public interface ClientUniqueData {
    /**
     * All unique-data must implement consistent {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
     *
     * @return this unique-data's hash-code.
     */
    int hashCode();

    /**
     * All unique-data must implement consistent {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
     *
     * @param other the other object this is being compared to.
     * @return whether this object and the other object are equals.
     */
    boolean equals(Object other);
}
