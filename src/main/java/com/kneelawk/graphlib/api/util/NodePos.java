package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;

/**
 * Represents a positioned block node.
 *
 * @param pos  the block pos.
 * @param node the block node.
 */
public record NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
    /**
     * Creates a positioned block node representation.
     *
     * @param pos  the block pos.
     * @param node the block node.
     */
    public NodePos(@NotNull BlockPos pos, @NotNull BlockNode node) {
        this.pos = pos.toImmutable();
        this.node = node;
    }
}
