package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeWrapper;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WireConnectionFilter extends FullWireConnectionFilter, SidedWireConnectionFilter {
    boolean accepts(@NotNull BlockNode self, @NotNull BlockNode other);

    @Override
    default boolean canConnect(@NotNull FullWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                               @NotNull Direction onSide, @Nullable Direction wireSide,
                               @NotNull Node<BlockNodeWrapper<?>> other) {
        return accepts(self, other.data().node());
    }

    @Override
    default boolean canConnect(@NotNull SidedWireBlockNode self, @NotNull ServerWorld world, @NotNull BlockPos pos,
                               @NotNull Direction inDirection, @NotNull WireConnectionType connectionType,
                               @NotNull Node<BlockNodeWrapper<?>> other) {
        return accepts(self, other.data().node());
    }
}
