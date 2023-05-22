package com.kneelawk.graphlib.api.node;

import java.util.Objects;

/**
 * Represents a discovered block node.
 *
 * @param nodeKey  the node's unique data that, when combined with the block-pos, represents the node's key.
 * @param nodeCreator a creator for if a new node needs to be created as the given location.
 */
public record BlockNodeDiscovery(NodeKey nodeKey, BlockNodeFactory nodeCreator) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockNodeDiscovery discovery = (BlockNodeDiscovery) o;
        return Objects.equals(nodeKey, discovery.nodeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeKey);
    }
}
