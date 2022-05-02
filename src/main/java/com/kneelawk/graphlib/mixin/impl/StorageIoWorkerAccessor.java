package com.kneelawk.graphlib.mixin.impl;

import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(StorageIoWorker.class)
public interface StorageIoWorkerAccessor {
    @Invoker("<init>")
    static StorageIoWorker create(Path path, boolean bl, String string) {
        throw new RuntimeException("StorageIoWorkerAccessor not mixed in.");
    }
}
