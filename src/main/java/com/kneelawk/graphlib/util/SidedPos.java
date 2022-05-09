package com.kneelawk.graphlib.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record SidedPos(BlockPos pos, Direction side) {
}
