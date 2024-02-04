/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.fabric.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

@SuppressWarnings("unused")
public class GraphLibFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GLLog.setGameDir(FabricLoader.getInstance().getGameDir());

        GLLog.info("Initializing GraphLib...");

        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, GraphLibImpl.UNIVERSE_IDENTIFIER, GraphLibImpl.UNIVERSE);

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
