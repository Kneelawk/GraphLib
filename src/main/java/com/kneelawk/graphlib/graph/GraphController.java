package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.world.UnloadingRegionBasedStorage;
import net.minecraft.server.world.ServerWorld;

import java.nio.file.Path;

public class GraphController extends UnloadingRegionBasedStorage<GraphControllerChunk> {
    private final ServerWorld world;

    private final Path graphsDir;

    public GraphController(ServerWorld world, Path path, boolean syncChunkWrites) {
        super(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites, GraphControllerChunk::new, GraphControllerChunk::new);
        this.world = world;
        graphsDir = path.resolve(Constants.GRAPHS_DIRNAME);
    }
}
