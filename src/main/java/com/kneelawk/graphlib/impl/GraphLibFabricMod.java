package com.kneelawk.graphlib.impl;

import com.kneelawk.graphlib.api.GraphLib;
import com.kneelawk.graphlib.impl.graph.simple.SimpleBlockGraphController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unused")
public class GraphLibFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GLLog.setupLogging(FabricLoader.getInstance().getGameDir());

        GLLog.info("Initializing GraphLib...");

        GraphLibImpl.register();

        GraphLibCommonNetworking.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GraphLibImpl.registerCommands(dispatcher));

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            try {
                GraphLibImpl.getSimpleController(world).onWorldChunkLoad(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error loading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            try {
                SimpleBlockGraphController controller = GraphLibImpl.getSimpleController(world);
                controller.saveChunk(chunk.getPos());
                controller.onWorldChunkUnload(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            try {
                GraphLibImpl.getSimpleController(world).tick();
            } catch (Exception e) {
                GLLog.error("Error ticking BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                GraphLibImpl.getSimpleController(world).close();
            } catch (Exception e) {
                GLLog.error("Error closing BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });

        GLLog.info("GraphLib Initialized.");
    }
}
