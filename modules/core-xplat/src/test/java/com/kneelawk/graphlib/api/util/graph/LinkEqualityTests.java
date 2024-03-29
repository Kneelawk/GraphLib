package com.kneelawk.graphlib.api.util.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LinkEqualityTests {
    @Test
    public void testLinksEqualBothWays() {
        Node<String, String> a = new Node<>("A");
        Node<String, String> b = new Node<>("B");

        Link<String, String> aToB = new Link<>(a, b, "C");
        Link<String, String> bToA = new Link<>(b, a, "C");

        assertEquals("The links should equal each other", aToB, bToA);
        assertEquals("The links' hashCodes should equal each other", aToB.hashCode(), bToA.hashCode());
    }

    @Test
    public void testLinksWithDifferentKeys() {
        Node<String, String> a = new Node<>("A");
        Node<String, String> b = new Node<>("B");

        Link<String, String> cLink = new Link<>(a, b, "C");
        Link<String, String> dLink = new Link<>(a, b, "D");

        assertNotEquals("The links should not be equal", cLink, dLink);
    }
}
