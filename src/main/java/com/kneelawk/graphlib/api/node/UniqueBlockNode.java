package com.kneelawk.graphlib.api.node;

/**
 * Describes a block node that is unique in its contents, implements consistent {@link Object#equals(Object)} and
 * {@link Object#hashCode()} functions, and that can be created on the fly and used as a key.
 */
public interface UniqueBlockNode extends BlockNode, UniqueData {
}
