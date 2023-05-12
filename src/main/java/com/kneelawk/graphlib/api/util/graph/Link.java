package com.kneelawk.graphlib.api.util.graph;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

/**
 * Link in a general purpose graph data structure.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param <T>    the type of data contained in each node.
 */
public record Link<T>(@NotNull Node<T> first, @NotNull Node<T> second) {
    public boolean contains(@NotNull Node<T> node) {
        return Objects.equals(first, node) || Objects.equals(second, node);
    }

    public @NotNull Node<T> other(@NotNull Node<T> node) {
        if (Objects.equals(first, node)) {
            return second;
        } else {
            return first;
        }
    }
}
