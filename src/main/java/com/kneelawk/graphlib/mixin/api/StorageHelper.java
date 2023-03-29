package com.kneelawk.graphlib.mixin.api;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import com.kneelawk.graphlib.mixin.impl.StorageIoWorkerAccessor;

public class StorageHelper {
    public static @NotNull StorageIoWorker newWorker(@NotNull Path path, boolean syncChunkWrites,
                                                     @NotNull String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static @NotNull SimpleBlockGraphController getController(@NotNull ServerWorld world) {
        return ((BlockGraphControllerAccess) world.getChunkManager().delegate).graphlib_getGraphController();
    }
}
