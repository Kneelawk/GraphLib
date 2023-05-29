package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

public interface LinkKeyDecoder {
    @Nullable LinkKey decode(@Nullable NbtElement tag);
}
