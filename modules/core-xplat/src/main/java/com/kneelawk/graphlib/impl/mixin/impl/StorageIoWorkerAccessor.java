package com.kneelawk.graphlib.impl.mixin.impl;

import java.nio.file.Path;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(IOWorker.class)
public interface StorageIoWorkerAccessor {
    @Invoker("<init>")
    static IOWorker create(RegionStorageInfo storageKey, Path directory, boolean dsync) {
        throw new RuntimeException("StorageIoWorkerAccessor not mixed in.");
    }
}
