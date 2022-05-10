package com.kneelawk.graphlib;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@SuppressWarnings("unused")
public class GraphLibQuiltMod implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        GraphLib.register();

        ServerChunkEvents.CHUNK_LOAD.register(
                (world, chunk) -> GraphLib.getController(world).onWorldChunkLoad(chunk.getPos()));
        ServerChunkEvents.CHUNK_UNLOAD.register(
                (world, chunk) -> GraphLib.getController(world).onWorldChunkUnload(chunk.getPos()));
        ServerWorldTickEvents.END.register((server, world) -> GraphLib.getController(world).tick());
    }
}
