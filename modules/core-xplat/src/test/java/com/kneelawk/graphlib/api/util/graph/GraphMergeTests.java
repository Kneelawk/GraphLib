package com.kneelawk.graphlib.api.util.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals(3, graphA.size(), "Graph A should have 3 nodes.");
        assertEquals(0, graphB.size(), "Graph B should have 0 nodes.");

        assertTrue(graphA.contains(a) && graphA.contains(b) && graphA.contains(c),
            "Graph A should contain A, B, and C.");
        assertTrue(b.connections().contains(link) && c.connections().contains(link), "B and C should stay linked.");
    }
}
