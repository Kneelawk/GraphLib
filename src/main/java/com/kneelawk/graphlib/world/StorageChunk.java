package com.kneelawk.graphlib.world;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public interface StorageChunk {
    void toNbt(@NotNull NbtCompound nbt);
}
