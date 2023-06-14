package com.kneelawk.graphlib.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;

import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;
import com.kneelawk.graphlib.impl.net.GLDebugNet;

@SuppressWarnings("unused")
public class GraphLibFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GLLog.setGameDir(FabricLoader.getInstance().getGameDir());

        GLLog.info("Initializing GraphLib...");

        GraphLibImpl.register();

        GLDebugNet.init();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, context, environment) -> GraphLibImpl.registerCommands(dispatcher, context));

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            try {
                StorageHelper.getStorage(world).onWorldChunkLoad(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error loading chunk in GraphWorldStorage. World: '{}'/{}, Chunk: {}", world,
                    world.getRegistryKey().getValue(), chunk.getPos(), e);
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            try {
                ServerGraphWorldStorage storage = StorageHelper.getStorage(world);
                storage.saveChunk(chunk.getPos());
                storage.onWorldChunkUnload(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in GraphWorldStorage. World: '{}'/{}, Chunk: {}", world,
                    world.getRegistryKey().getValue(), chunk.getPos(), e);
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            try {
                StorageHelper.getStorage(world).tick();
            } catch (Exception e) {
                GLLog.error("Error ticking GraphWorldStorage. World: '{}'/{}", world,
                    world.getRegistryKey().getValue(), e);
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                StorageHelper.getStorage(world).close();
            } catch (Exception e) {
                GLLog.error("Error closing GraphWorldStorage. World: '{}'/{}", world,
                    world.getRegistryKey().getValue(), e);
            }
        });

        GLLog.info("GraphLib Initialized.");
    }
}
