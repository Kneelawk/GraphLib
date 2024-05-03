package com.kneelawk.graphlib.impl.mixin.api;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.StorageKey;

import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.impl.StorageIoWorkerAccessor;

public class StorageHelper {
    public static @NotNull StorageIoWorker newWorker(@NotNull StorageKey key, @NotNull Path directory, boolean dsync) {
        return StorageIoWorkerAccessor.create(key, directory, dsync);
    }

    public static @NotNull ServerGraphWorldStorage getStorage(@NotNull ServerWorld world) {
        return ((GraphWorldStorageAccess) world.getChunkManager().threadedAnvilChunkStorage).graphlib_getGraphWorldStorage();
    }
}
