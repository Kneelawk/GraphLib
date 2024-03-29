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

package com.kneelawk.graphlib.neoforge.impl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.api.GraphLib;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

@Mod("graphlib")
@SuppressWarnings("unused")
public class GraphLibNeoForgeMod {
    public GraphLibNeoForgeMod(IEventBus eventBus) {
        GLLog.setGameDir(FMLPaths.GAMEDIR.get());

        GLLog.info("Initializing GraphLib...");

        eventBus.addListener(this::onPostInit);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnload);
        NeoForge.EVENT_BUS.addListener(this::onLevelTick);
        NeoForge.EVENT_BUS.addListener(this::onLevelUnload);
    }

    public void onPostInit(FMLLoadCompleteEvent event) {
        GLLog.info("GraphLib Initialized.");
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        GraphLibCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerWorld world) {
            ChunkPos chunk = event.getChunk().getPos();
            try {
                StorageHelper.getStorage(world).onWorldChunkLoad(chunk);
            } catch (Exception e) {
                GLLog.error("Error loading chunk in GraphWorldStorage. World: '{}'/{}, Chunk: {}", world,
                    world.getRegistryKey().getValue(), chunk, e);
            }
        }
    }

    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerWorld world) {
            ChunkPos chunk = event.getChunk().getPos();
            try {
                ServerGraphWorldStorage storage = StorageHelper.getStorage(world);
                storage.saveChunk(chunk);
                storage.onWorldChunkUnload(chunk);
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in GraphWorldStorage. World: '{}'/{}, Chunk: {}", world,
                    world.getRegistryKey().getValue(), chunk, e);
            }
        }
    }

    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerWorld world) {
            try {
                StorageHelper.getStorage(world).tick();
            } catch (Exception e) {
                GLLog.error("Error ticking GraphWorldStorage. World: '{}'/{}", world, world.getRegistryKey().getValue(),
                    e);
            }
        }
    }

    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerWorld world) {
            try {
                StorageHelper.getStorage(world).close();
            } catch (Exception e) {
                GLLog.error("Error closing GraphWorldStorage. World: '{}'/{}", world, world.getRegistryKey().getValue(),
                    e);
            }
        }
    }
}
