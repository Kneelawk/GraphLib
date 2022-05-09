package com.kneelawk.graphlib.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public record SidedPos(@NotNull BlockPos pos, @NotNull Direction side) {
}
