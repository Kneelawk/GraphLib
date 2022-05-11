package com.kneelawk.graphlib.graph.struct;

import org.junit.Test;

import static org.junit.Assert.*;

public class GraphSplitTests {
    @Test
    public void simpleSplitTest() {
        Graph<String> graph = new Graph<>();

        var a = graph.add("A");
        var b = graph.add("B");
        graph.link(a, b);

        var c = graph.add("C");

        var newGraphs = graph.split();

        assertEquals("There should be one new graph.", 1, newGraphs.size());

        Graph<String> newGraph = newGraphs.get(0);

        assertTrue("Either the new graph or the old graph should contain C.",
                newGraph.contains(c) || graph.contains(c));
        assertTrue("Either the new graph or the old graph should contain both A and B.",
                newGraph.contains(a, b) || graph.contains(a, b));

        assertFalse("The same graph should not contain both A and C.", newGraph.contains(a, c) || graph.contains(a, c));
    }
}
