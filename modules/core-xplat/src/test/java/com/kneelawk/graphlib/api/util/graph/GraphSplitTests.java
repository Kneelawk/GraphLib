package com.kneelawk.graphlib.api.util.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphSplitTests {
    private static final Object PRESENT = new Object();

    @Test
    public void simpleSplitTest() {
        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b, PRESENT);

        var c = graph.add("C");

        var newGraphs = graph.split();

        assertEquals(1, newGraphs.size(), "There should be one new graph.");

        Graph<String, Object> newGraph = newGraphs.getFirst();

        assertTrue(newGraph.contains(c) || graph.contains(c),
            "Either the new graph or the old graph should contain C.");
        assertTrue(newGraph.contains(a, b) || graph.contains(a, b),
            "Either the new graph or the old graph should contain both A and B.");

        assertFalse(newGraph.contains(a, c) || graph.contains(a, c), "The same graph should not contain both A and C.");
    }

    @Test
    public void splitSizePreferenceTest() {
        // This test and the next one check to make sure the graph always keeps the largest bunch of nodes

        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b, PRESENT);

        var c = graph.add("C");

        var newGraphs = graph.split();

        assertEquals(1, newGraphs.size(), "There should be one new graph.");

        Graph<String, Object> newGraph = newGraphs.getFirst();

        assertTrue(newGraph.contains(c), "The new graph should contain C.");
        assertTrue(graph.contains(a, b), "The old graph should contain both A and B.");
    }

    @Test
    public void splitSizePreferenceTest2() {
        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b, PRESENT);

        var c = graph.add("C");
        var d = graph.add("D");
        var e = graph.add("E");
        graph.link(c, d, PRESENT);
        graph.link(d, e, PRESENT);

        var newGraphs = graph.split();

        assertEquals(1, newGraphs.size(), "There should be one new graph.");

        Graph<String, Object> newGraph = newGraphs.getFirst();

        assertTrue(graph.contains(c, d, e), "The old graph should contain C, D, and E.");
        assertTrue(newGraph.contains(a, b), "The new graph should contain both A and B.");
    }
}
