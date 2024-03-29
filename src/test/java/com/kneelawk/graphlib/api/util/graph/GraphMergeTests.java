package com.kneelawk.graphlib.api.util.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphMergeTests {
    private static final Object PRESENT = new Object();

    @Test
    public void simpleMergeTest() {
        Graph<String, Object> graphA = new Graph<>();
        Graph<String, Object> graphB = new Graph<>();

        var a = graphA.add("A");
        var b = graphB.add("B");
        var c = graphB.add("C");
        var link = graphB.link(b, c, PRESENT);

        graphA.join(graphB);

        assertEquals("Graph A should have 3 nodes.", 3, graphA.size());
        assertEquals("Graph B should have 0 nodes.", 0, graphB.size());

        assertTrue("Graph A should contain A, B, and C.",
            graphA.contains(a) && graphA.contains(b) && graphA.contains(c));
        assertTrue("B and C should stay linked.", b.connections().contains(link) && c.connections().contains(link));
    }
}
