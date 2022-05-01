package com.kneelawk.wirenetlib.wire;

import com.kneelawk.wirenetlib.Constants;
import com.kneelawk.wirenetlib.world.UnloadingRegionBasedStorage;
import net.minecraft.server.world.ServerWorld;

import java.nio.file.Path;

public class WireNetworkController extends UnloadingRegionBasedStorage<WireNetworkChunk> {
    private final ServerWorld world;

    private final Path graphsDir;

    public WireNetworkController(ServerWorld world, Path path, boolean syncChunkWrites) {
        super(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites, WireNetworkChunk::new, WireNetworkChunk::new);
        this.world = world;
        graphsDir = path.resolve(Constants.GRAPHS_DIRNAME);
    }
}
