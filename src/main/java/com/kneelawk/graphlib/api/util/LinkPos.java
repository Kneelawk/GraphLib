package com.kneelawk.graphlib.api.util;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Represents a positioned unique link in a way that can be looked up.
 * <p>
 * Note: this type is non-directional. A link from 'A' to 'B' is the same as a link from 'B' to 'A'. The
 * {@link #equals(Object)} and {@link #hashCode()} methods reflect this.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param key    the key of this link that makes it unique among all the links between the same two nodes.
 */
public record LinkPos(NodePos first, NodePos second, LinkKey key) {
    /**
     * Creates a new link pos from raw positions, nodes, and the link key.
     *
     * @param firstPos   the block position of the first end of this link.
     * @param firstNode  the block node at the first end of this link.
     * @param secondPos  the block position of the second end of this link.
     * @param secondNode the block node at the second end of this link.
     * @param key        the key of this link.
     */
    public LinkPos(BlockPos firstPos, BlockNode firstNode, BlockPos secondPos, BlockNode secondNode, LinkKey key) {
        this(new NodePos(firstPos, firstNode), new NodePos(secondPos, secondNode), key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkPos linkKey = (LinkPos) o;

        if (!key.equals(linkKey.key)) return false;

        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = result ^ second.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}
