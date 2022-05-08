package com.kneelawk.graphlib.mixin.api;

import com.kneelawk.graphlib.graph.BlockGraphController;
import com.kneelawk.graphlib.mixin.impl.StorageIoWorkerAccessor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import java.nio.file.Path;

public class StorageHelper {
    public static StorageIoWorker newWorker(Path path, boolean syncChunkWrites, String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static BlockGraphController getController(ServerWorld world) {
        return ((BlockGraphControllerAccess) world.getChunkManager().threadedAnvilChunkStorage).graphlib_getGraphController();
    }
}
