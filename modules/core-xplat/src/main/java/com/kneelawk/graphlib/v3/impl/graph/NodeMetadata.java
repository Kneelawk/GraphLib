package com.kneelawk.graphlib.v3.impl.graph;

public class NodeMetadata {
    private long graphId;

    public NodeMetadata(long graphId) {
        this.graphId = graphId;
    }

    public long getGraphId() {
        return graphId;
    }

    public void setGraphId(long graphId) {
        this.graphId = graphId;
    }
}
