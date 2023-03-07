package com.kneelawk.graphlib.impl.mixin.api;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphWorld;
import com.kneelawk.graphlib.impl.mixin.impl.StorageIoWorkerAccessor;

public class StorageHelper {
    public static @NotNull StorageIoWorker newWorker(@NotNull Path path, boolean syncChunkWrites,
                                                     @NotNull String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static @NotNull SimpleGraphWorld getController(@NotNull ServerWorld world) {
        return ((BlockGraphControllerAccess) world.getChunkManager().threadedAnvilChunkStorage).graphlib_getGraphController();
    }
}
