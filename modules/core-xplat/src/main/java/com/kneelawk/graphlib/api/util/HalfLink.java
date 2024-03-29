package com.kneelawk.graphlib.api.util;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Describes a link from the perspective of an existing node.
 *
 * @param key   the key of the link this describes.
 * @param other the other node in the link this describes.
 */
public record HalfLink(LinkKey key, NodeHolder<BlockNode> other) {

    /**
     * Reverses this half link, replacing other with newOther.
     *
     * @param newOther the new end of the half link.
     * @return a half link pointing at the new other node.
     */
    public HalfLink reverse(NodeHolder<BlockNode> newOther) {
        return new HalfLink(key, newOther);
    }

    /**
     * Creates a link pos describing this link, including the given perspective.
     *
     * @param perspective the originator of this half link.
     * @return a link pos containing both the given node and this half link.
     */
    public LinkPos toLinkPos(NodePos perspective) {
        return new LinkPos(perspective, other.getPos(), key);
    }
}
