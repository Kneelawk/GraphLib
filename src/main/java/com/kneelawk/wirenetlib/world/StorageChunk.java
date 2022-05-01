package com.kneelawk.wirenetlib.world;

import net.minecraft.nbt.NbtCompound;

public interface StorageChunk {
    void toNbt(NbtCompound nbt);
}
