package com.kneelawk.graphlib.impl.mixin.api;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;
import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphWorld;
import com.kneelawk.graphlib.impl.mixin.impl.StorageIoWorkerAccessor;

public class StorageHelper {
    public static @NotNull StorageIoWorker newWorker(@NotNull Path path, boolean syncChunkWrites,
                                                     @NotNull String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static @NotNull GraphWorldStorage getStorage(@NotNull ServerWorld world) {
        return ((GraphWorldStorageAccess) world.getChunkManager().delegate).graphlib_getGraphWorldStorage();
    }
}
