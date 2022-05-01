package com.kneelawk.wirenetlib.wire;

import com.kneelawk.wirenetlib.world.UnloadingRegionBasedStorage;
import net.minecraft.server.world.ServerWorld;

import java.nio.file.Path;

public class WireNetworkController extends UnloadingRegionBasedStorage<WireNetworkChunk> {
    private final ServerWorld world;

    public WireNetworkController(ServerWorld world, Path path, boolean syncChunkWrites) {
        super(path, syncChunkWrites);
        this.world = world;
    }
}
