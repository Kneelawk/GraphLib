package com.kneelawk.graphlib.graph;

import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public interface BlockNodeDecoder {
    @Nullable BlockNode createBlockNodeFromTag(@Nullable NbtElement tag);
}
