package com.kneelawk.graphlib.api.util.graph;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GraphUnlinkTests {
    private static final Object PRESENT = new Object();

    @Test
    public void graphUnlinkTest() {
        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A", PRESENT);
        graph.add("B", PRESENT);

        graph.link("A", "B");

        assertTrue("A's connections should include B", a.connections().containsKey("B"));

        graph.unlink("A", "B");

        assertFalse("A's connections should no longer include B", a.connections().containsKey("B"));
    }

    @Test
    public void graphUnlinkBackwardsTest() {
        Graph<String, Object> graph = new Graph<>();

        var a = graph.add("A", PRESENT);
        graph.add("B", PRESENT);

        graph.link("A", "B");

        assertTrue("A's connections should include B", a.connections().containsKey("B"));

        graph.unlink("B", "A");

        assertFalse("A's connections should no longer include B", a.connections().containsKey("B"));
    }
}
