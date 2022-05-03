package com.kneelawk.graphlib.graph;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// Translated from 2xsaiko's HCTM-Base Graph code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/graph/Graph.kt

public record Link<T>(@NotNull Node<T> first, @NotNull Node<T> second) {
    public boolean contains(@NotNull Node<T> node) {
        return Objects.equals(first, node) || Objects.equals(second, node);
    }

    public Node<T> other(@NotNull Node<T> node) {
        if (Objects.equals(first, node)) {
            return first;
        } else {
            return second;
        }
    }
}
