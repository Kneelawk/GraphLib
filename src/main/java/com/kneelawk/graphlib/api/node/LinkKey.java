package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.util.graph.Link;

/**
 * Describes how a link is unique.
 * <p>
 * <b>Note that this is <u>not</u> direction dependent.</b> Link keys' {@link #equals(Object)} and {@link #hashCode()}
 * functions act the same no matter which node is first and which is second.
 *
 * @param first  the key of the first node in this link.
 * @param second the key of the second node in this link.
 */
public record LinkKey(@NotNull PosNodeKey first, @NotNull PosNodeKey second) {
    /**
     * Checks to see if this link contains the given key.
     *
     * @param key the key to see if this link contains.
     * @return whether this link contains the given key.
     */
    public boolean contains(@NotNull PosNodeKey key) {
        return first.equals(key) || second.equals(key);
    }

    /**
     * Returns the node-key at the opposite end of this link from the given key.
     *
     * @param key the key to get the node-key opposite of.
     * @return the node-ley at the other end of this link from the given key.
     */
    public @NotNull PosNodeKey other(@NotNull PosNodeKey key) {
        if (first.equals(key)) {
            return second;
        } else {
            return first;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkKey linkKey = (LinkKey) o;

        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = result ^ second.hashCode();
        return result;
    }

    /**
     * Creates a new link-key from a link.
     *
     * @param link the link to get the link-key of.
     * @return a new link-key describing the given link.
     */
    public static LinkKey from(Link<PosNodeKey, ?> link) {
        return new LinkKey(link.first().key(), link.second().key());
    }

    /**
     * Creates a new link-key from a node-connection.
     *
     * @param conn the connection to get the link-key of.
     * @return a new link-key describing the given connection.
     */
    public static LinkKey from(NodeLink conn) {
        return new LinkKey(conn.getFirst().toNodeKey(), conn.getSecond().toNodeKey());
    }
}
