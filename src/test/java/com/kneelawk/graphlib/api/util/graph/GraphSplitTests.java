package com.kneelawk.graphlib.api.util.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GraphSplitTests {
    private static final Object PRESENT = new Object();

    @Test
    public void simpleSplitTest() {
        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A", PRESENT);
        var b = graph.add("B", PRESENT);
        graph.link(a, b);

        graph.add("C", PRESENT);

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String, Object> newGraph = newGraphs.get(0);

        assertTrue("Either the new graph or the old graph should contain C.",
            newGraph.containsKey("C") || graph.containsKey("C"));
        assertTrue("Either the new graph or the old graph should contain both A and B.",
            newGraph.containsKeys("A", "B") || graph.containsKeys("A", "B"));

        assertFalse("The same graph should not contain both A and C.", newGraph.containsKeys("A", "C") || graph.containsKeys("A", "C"));
    }

    @Test
    public void splitSizePreferenceTest() {
        // This test and the next one check to make sure the graph always keeps the largest bunch of nodes

        Graph<String> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b);

        var c = graph.add("C");

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String> newGraph = newGraphs.get(0);

        assertTrue("The new graph should contain C.", newGraph.contains(c));
        assertTrue("The old graph should contain both A and B.", graph.contains(a, b));
    }

    @Test
    public void splitSizePreferenceTest2() {
        Graph<String> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b);

        var c = graph.add("C");
        var d = graph.add("D");
        var e = graph.add("E");
        graph.link(c, d);
        graph.link(d, e);

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String> newGraph = newGraphs.get(0);

        assertTrue("The old graph should contain C, D, and E.", graph.contains(c, d, e));
        assertTrue("The new graph should contain both A and B.", newGraph.contains(a, b));
    }
}
