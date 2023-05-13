package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

/**
 * Simple {@link Direction} utilities.
 */
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

    /**
     * Gets all the directions perpendicular to the given direction.
     *
     * @param side the direction to find directions perpendicular to.
     * @return an array of directions perpendicular to the given direction.
     */
    @Contract(pure = true)
    public static @NotNull Direction[] perpendiculars(@NotNull Direction side) {
        return PERPENDICULARS[side.getId()];
    }
}
