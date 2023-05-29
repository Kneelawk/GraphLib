package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public interface LinkKey {
    @NotNull Identifier getTypeId();

    @Nullable NbtElement toTag();
}
