package com.kneelawk.graphlib;

import com.kneelawk.graphlib.mixin.api.StorageHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

@SuppressWarnings("unused")
public class GraphLibMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GraphLib.register();

        ServerChunkEvents.CHUNK_LOAD.register(
                (world, chunk) -> StorageHelper.getController(world).onWorldChunkLoad(chunk.getPos()));
        ServerChunkEvents.CHUNK_UNLOAD.register(
                (world, chunk) -> StorageHelper.getController(world).onWorldChunkUnload(chunk.getPos()));
        ServerTickEvents.END_WORLD_TICK.register(world -> StorageHelper.getController(world).tick());
    }
}
