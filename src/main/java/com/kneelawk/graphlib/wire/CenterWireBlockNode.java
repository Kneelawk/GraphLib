package com.kneelawk.graphlib.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Node;

public interface CenterWireBlockNode extends BlockNode {
    default boolean canConnect(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Direction onSide,
                               @NotNull Node<BlockNodeHolder> self, @NotNull Node<BlockNodeHolder> other) {
        return true;
    }
}
