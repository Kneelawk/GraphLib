package com.kneelawk.wirenetlib.mixin.api;

import com.kneelawk.wirenetlib.mixin.impl.StorageIoWorkerAccessor;
import com.kneelawk.wirenetlib.wire.WireNetworkController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.StorageIoWorker;

import java.nio.file.Path;

public class StorageHelper {
    public static StorageIoWorker newWorker(Path path, boolean syncChunkWrites, String name) {
        return StorageIoWorkerAccessor.create(path, syncChunkWrites, name);
    }

    public static WireNetworkController getController(ServerWorld world) {
        return ((WireNetworkControllerAccess) world.getChunkManager().threadedAnvilChunkStorage).wirenetlib_getWireNetworkController();
    }
}
