package com.kneelawk.graphlib.node;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface BlockNode {
    Identifier getTypeId();

    @Nullable NbtElement toTag();

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);
}
