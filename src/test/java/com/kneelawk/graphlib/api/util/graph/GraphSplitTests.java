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

        graph.add("A", PRESENT);
        graph.add("B", PRESENT);
        graph.link("A", "B");

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

        Graph<String, Object> graph = new Graph<>();

        graph.add("A", PRESENT);
        graph.add("B", PRESENT);
        graph.link("A", "B");

        graph.add("C", PRESENT);

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String, Object> newGraph = newGraphs.get(0);

        assertTrue("The new graph should contain C.", newGraph.containsKey("C"));
        assertTrue("The old graph should contain both A and B.", graph.containsKeys("A", "B"));
    }

    @Test
    public void splitSizePreferenceTest2() {
        Graph<String, Object> graph = new Graph<>();

        graph.add("A", PRESENT);
        graph.add("B", PRESENT);
        graph.link("A", "B");

        graph.add("C", PRESENT);
        graph.add("D", PRESENT);
        graph.add("E", PRESENT);
        graph.link("C", "D");
        graph.link("D", "E");

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String, Object> newGraph = newGraphs.get(0);

        assertTrue("The old graph should contain C, D, and E.", graph.containsKeys("C", "D", "E"));
        assertTrue("The new graph should contain both A and B.", newGraph.containsKeys("A", "B"));
    }
}
