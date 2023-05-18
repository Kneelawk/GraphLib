package com.kneelawk.graphlib.api.node;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a discovered block node.
 *
 * @param uniqueData  the node's unique data that, when combined with the block-pos, represents the node's key.
 * @param nodeCreator a creator for if a new node needs to be created as the given location.
 */
public record BlockNodeDiscovery(UniqueData uniqueData, Supplier<BlockNode> nodeCreator) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockNodeDiscovery discovery = (BlockNodeDiscovery) o;
        return Objects.equals(uniqueData, discovery.uniqueData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueData);
    }

    /**
     * Creates a new block discovery holding a unique block node that can be used as both key and value.
     *
     * @param unique the unique block node.
     * @return a new block node discovery containing the unique block node.
     */
    public static BlockNodeDiscovery ofUniqueBlockNode(UniqueBlockNode unique) {
        return new BlockNodeDiscovery(unique, () -> unique);
    }
}
