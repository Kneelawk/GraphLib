package com.kneelawk.graphlib.node;

import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public interface BlockNodeDecoder {
    @Nullable BlockNode createBlockNodeFromTag(@Nullable NbtElement tag);
}
