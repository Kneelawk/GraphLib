package com.kneelawk.graphlib.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public sealed interface PosWrapper {
    record Block(@NotNull BlockPos pos) implements PosWrapper {
        @Override
        public @NotNull BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public @NotNull SidedPos getSidedPos() {
            throw new IllegalStateException("Called getSidePos on a non-sided PosWrapper.Block");
        }

        @Override
        public @NotNull Direction getSide() {
            throw new IllegalStateException("Called getSide on a non-sided PosWrapper.Block");
        }

        @Override
        public boolean isSided() {
            return false;
        }
    }

    record Sided(@NotNull SidedPos pos) implements PosWrapper {
        @Override
        public @NotNull BlockPos getBlockPos() {
            return pos.pos();
        }

        @Override
        public @NotNull SidedPos getSidedPos() {
            return pos;
        }

        @Override
        public @NotNull Direction getSide() {
            return pos.side();
        }

        @Override
        public boolean isSided() {
            return true;
        }
    }

    @NotNull BlockPos getBlockPos();

    @NotNull SidedPos getSidedPos();

    @NotNull Direction getSide();

    boolean isSided();
}
