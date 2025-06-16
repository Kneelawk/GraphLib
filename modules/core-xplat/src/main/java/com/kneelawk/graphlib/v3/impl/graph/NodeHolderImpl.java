package com.kneelawk.graphlib.v3.impl.graph;

import com.kneelawk.graphlib.v3.api.datastructure.KeyedNode;
import com.kneelawk.graphlib.v3.api.graph.GraphView;
import com.kneelawk.graphlib.v3.api.graph.NodeHolder;
import com.kneelawk.graphlib.v3.api.graph.user.LinkKey;
import com.kneelawk.graphlib.v3.api.pos.NodePos;

/**
 * Holds a graph node and all associated information.
 *
 * @param keyedNode the keyed node that holds the node pos and connection information.
 * @param graphView the graph view that the node is from.
 */
public record NodeHolderImpl(KeyedNode<NodePos, NodeMetadata, LinkKey, LinkMetadata> keyedNode, GraphView graphView)
    implements NodeHolder {
    @Override
    public NodePos pos() {
        return keyedNode.key();
    }

    @Override
    public long graphId() {
        return keyedNode.value().getGraphId();
    }
}
