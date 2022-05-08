package com.kneelawk.graphlib.graph;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface BlockNode {
    Identifier getTypeId();

    @Nullable NbtElement toTag();

    void onChanged(ServerWorld world, BlockPos pos);

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);
}
