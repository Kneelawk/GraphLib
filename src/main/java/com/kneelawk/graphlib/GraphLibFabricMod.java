package com.kneelawk.graphlib;

import com.kneelawk.graphlib.graph.BlockGraphController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

@SuppressWarnings("unused")
public class GraphLibFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GraphLib.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> GraphLib.registerCommands(dispatcher));

        ServerChunkEvents.CHUNK_LOAD.register(
                (world, chunk) -> GraphLib.getController(world).onWorldChunkLoad(chunk.getPos()));
        ServerChunkEvents.CHUNK_UNLOAD.register(
                (world, chunk) -> {
                    BlockGraphController controller = GraphLib.getController(world);
                    controller.saveChunk(chunk.getPos());
                    controller.onWorldChunkUnload(chunk.getPos());
                });
        ServerTickEvents.END_WORLD_TICK.register(world -> GraphLib.getController(world).tick());
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                GraphLib.getController(world).close();
            } catch (Exception e) {
                GraphLib.log.error("Error closing BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
    }
}
