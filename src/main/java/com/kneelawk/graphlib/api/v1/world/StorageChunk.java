package com.kneelawk.graphlib.api.v1.world;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;

public interface StorageChunk {
    void toNbt(@NotNull NbtCompound nbt);
}
