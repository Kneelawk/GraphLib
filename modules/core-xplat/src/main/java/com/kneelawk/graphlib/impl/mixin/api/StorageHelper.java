package com.kneelawk.graphlib.impl.mixin.api;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.impl.StorageIoWorkerAccessor;

public class StorageHelper {
    public static @NotNull IOWorker newWorker(@NotNull RegionStorageInfo key, @NotNull Path directory, boolean dsync) {
        return StorageIoWorkerAccessor.create(key, directory, dsync);
    }

    public static @NotNull ServerGraphWorldStorage getStorage(@NotNull ServerLevel world) {
        return ((GraphWorldStorageAccess) world.getChunkSource().chunkMap).graphlib_getGraphWorldStorage();
    }
}
