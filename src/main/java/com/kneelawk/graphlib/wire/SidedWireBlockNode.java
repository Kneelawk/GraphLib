package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNodeWrapper;
import com.kneelawk.graphlib.graph.SidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public interface SidedWireBlockNode extends SidedBlockNode {
    default boolean canConnect(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Direction inDirection,
                               @NotNull WireConnectionType connectionType, @NotNull Node<BlockNodeWrapper<?>> other) {
        return true;
    }
}
