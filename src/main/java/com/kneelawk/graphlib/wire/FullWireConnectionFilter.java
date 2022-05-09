package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNodeWrapper;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FullWireConnectionFilter {
    boolean canConnect(@NotNull FullWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                       @NotNull Direction onSide, @Nullable Direction wireSide,
                       @NotNull Node<BlockNodeWrapper<?>> other);

    default FullWireConnectionFilter and(@NotNull FullWireConnectionFilter otherFilter) {
        return (self, world, pos, onSide, wireSide, other) -> canConnect(self, world, pos, onSide, wireSide, other) &&
                otherFilter.canConnect(self, world, pos, onSide, wireSide, other);
    }
}
