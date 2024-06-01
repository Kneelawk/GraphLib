package com.kneelawk.graphlib.api.util.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LinkEqualityTests {
    @Test
    public void testLinksEqualBothWays() {
        Node<String, String> a = new Node<>("A");
        Node<String, String> b = new Node<>("B");

        Link<String, String> aToB = new Link<>(a, b, "C");
        Link<String, String> bToA = new Link<>(b, a, "C");

        assertEquals(aToB, bToA, "The links should equal each other");
        assertEquals(aToB.hashCode(), bToA.hashCode(), "The links' hashCodes should equal each other");
    }

    @Test
    public void testLinksWithDifferentKeys() {
        Node<String, String> a = new Node<>("A");
        Node<String, String> b = new Node<>("B");

        Link<String, String> cLink = new Link<>(a, b, "C");
        Link<String, String> dLink = new Link<>(a, b, "D");

        assertNotEquals(cLink, dLink, "The links should not be equal");
    }
}
