package com.kneelawk.graphlib;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
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

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            try {
                GraphLib.getSimpleController(world).onWorldChunkLoad(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error loading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            try {
                SimpleBlockGraphController controller = GraphLib.getSimpleController(world);
                controller.saveChunk(chunk.getPos());
                controller.onWorldChunkUnload(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            try {
                GraphLib.getSimpleController(world).tick();
            } catch (Exception e) {
                GLLog.error("Error ticking BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                GraphLib.getSimpleController(world).close();
            } catch (Exception e) {
                GLLog.error("Error closing BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
    }
}
