package com.kneelawk.graphlib.mixin.api;

import com.kneelawk.graphlib.mixin.impl.StorageIoWorkerAccessor;
import com.kneelawk.graphlib.graph.GraphController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import java.nio.file.Path;

public class StorageHelper {
    public static StorageIoWorker newWorker(Path path, boolean syncChunkWrites, String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static GraphController getController(ServerWorld world) {
        return ((GraphControllerAccess) world.getChunkManager().threadedAnvilChunkStorage).graphlib_getGraphController();
    }
}
