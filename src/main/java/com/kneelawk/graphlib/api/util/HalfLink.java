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
}
