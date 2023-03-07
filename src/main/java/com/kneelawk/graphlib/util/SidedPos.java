package com.kneelawk.graphlib.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Represents both a block-position and the side of that block-position.
 *
 * @param pos  the block-position.
 * @param side the side of the block-position.
 */
public record SidedPos(@NotNull BlockPos pos, @NotNull Direction side) {
}
