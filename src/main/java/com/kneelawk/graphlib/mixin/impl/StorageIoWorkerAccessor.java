package com.kneelawk.graphlib.mixin.impl;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.storage.StorageIoWorker;

@Mixin(StorageIoWorker.class)
public interface StorageIoWorkerAccessor {
    @Invoker("<init>")
    static StorageIoWorker create(Path path, boolean bl, String string) {
        throw new RuntimeException("StorageIoWorkerAccessor not mixed in.");
    }
}
