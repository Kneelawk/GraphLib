package com.kneelawk.graphlib.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

public final class DirectionUtils {
    private DirectionUtils() {
    }

    private static final Direction[][] PERPENDICULARS;

    static {
        PERPENDICULARS = new Direction[6][];

        for (Direction side : Direction.values()) {
            Direction[] array = new Direction[4];

            int index = 0;
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().equals(side.getAxis())) {
                    array[index++] = dir;
                }
            }

            PERPENDICULARS[side.getId()] = array;
        }
    }

    @Contract(pure = true)
    public static @NotNull Direction[] perpendiculars(@NotNull Direction side) {
        return PERPENDICULARS[side.getId()];
    }
}
