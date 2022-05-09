package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNodeWrapper;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public interface SidedWireConnectionFilter {
    boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                       @NotNull Direction inDirection, @NotNull WireConnectionType connectionType,
                       @NotNull Node<BlockNodeWrapper<?>> other);

    default SidedWireConnectionFilter and(SidedWireConnectionFilter otherFilter) {
        return (self, world, pos, inDirection, connectionType, other) ->
                canConnect(self, world, pos, inDirection, connectionType, other) &&
                        otherFilter.canConnect(self, world, pos, inDirection, connectionType, other);
    }
}
