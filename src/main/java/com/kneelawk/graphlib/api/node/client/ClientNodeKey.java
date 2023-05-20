package com.kneelawk.graphlib.api.node.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.PosNodeKey;

/**
 * Client version of {@link PosNodeKey}.
 *
 * @param pos        the position of the block node.
 * @param uniqueData the unique data associated with the block node.
 */
public record ClientNodeKey(@NotNull BlockPos pos, @NotNull ClientUniqueData uniqueData) {
    /**
     * Constructs a new client-node-key.
     *
     * @param pos        the position of the block node. Note: this is made immutable.
     * @param uniqueData the unique data associated with the block node.
     */
    public ClientNodeKey(@NotNull BlockPos pos, @NotNull ClientUniqueData uniqueData) {
        this.pos = pos.toImmutable();
        this.uniqueData = uniqueData;
    }
}
