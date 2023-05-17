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

        graphA.add("A", PRESENT);
        var b = graphB.add("B", PRESENT);
        var c = graphB.add("C", PRESENT);
        var link = graphB.link(b, c);

        graphA.join(graphB);

        assertEquals("Graph A should have 3 nodes.", 3, graphA.size());
        assertEquals("Graph B should have 0 nodes.", 0, graphB.size());

        assertTrue("Graph A should contain A, B, and C.",
            graphA.containsKey("A") && graphA.containsKey("B") && graphA.containsKey("C"));
        assertTrue("B and C should stay linked.", b.connections().contains(link) && c.connections().contains(link));
    }
}
