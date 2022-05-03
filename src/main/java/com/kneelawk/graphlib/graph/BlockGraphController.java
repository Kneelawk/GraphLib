package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.util.ChunkSectionUnloadTimer;
import com.kneelawk.graphlib.world.UnloadingRegionBasedStorage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.nio.file.Path;

public class BlockGraphController extends UnloadingRegionBasedStorage<BlockGraphChunk> {
    /**
     * Graphs will unload 1 minute after their chunk unloads or their last use.
     */
    private static final int MAX_AGE = 20 * 60;

    private final ServerWorld world;

    private final ChunkSectionUnloadTimer timer;

    private final Path graphsDir;

    public BlockGraphController(ServerWorld world, Path path, boolean syncChunkWrites) {
        super(world, path.resolve(Constants.REGION_DIRNAME), syncChunkWrites, BlockGraphChunk::new,
                BlockGraphChunk::new);
        this.world = world;
        graphsDir = path.resolve(Constants.GRAPHS_DIRNAME);
        timer = new ChunkSectionUnloadTimer(world.getBottomSectionCoord(), world.getTopSectionCoord(), MAX_AGE);
    }

    @Override
    public void onWorldChunkLoad(ChunkPos pos) {
        super.onWorldChunkLoad(pos);
        timer.onWorldChunkLoad(pos);
    }

    @Override
    public void onWorldChunkUnload(ChunkPos pos) {
        super.onWorldChunkUnload(pos);
        timer.onWorldChunkUnload(pos);
    }

    @Override
    public void tick() {
        super.tick();
        timer.tick();
    }
}
