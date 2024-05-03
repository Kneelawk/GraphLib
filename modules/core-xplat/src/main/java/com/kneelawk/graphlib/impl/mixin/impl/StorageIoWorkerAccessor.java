package com.kneelawk.graphlib.impl.mixin.impl;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;

@Mixin(StorageIoWorker.class)
public interface StorageIoWorkerAccessor {
    @Invoker("<init>")
    static StorageIoWorker create(StorageKey storageKey, Path directory, boolean dsync) {
        throw new RuntimeException("StorageIoWorkerAccessor not mixed in.");
    }
}
