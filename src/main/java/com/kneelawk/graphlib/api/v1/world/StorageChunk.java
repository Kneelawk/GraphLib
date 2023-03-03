package com.kneelawk.graphlib.api.v1.world;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public interface StorageChunk {
    void toNbt(@NotNull NbtCompound nbt);
}
