package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNode;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FullWireBlockNode extends BlockNode {
    boolean canConnect(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull Direction onSide,
                       @Nullable Direction wireSide);
}
