package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;

public class ServerGraphWorldStorage implements GraphWorldStorage, AutoCloseable {
    private final Map<Identifier, ServerGraphWorldImpl> worlds = new Object2ObjectLinkedOpenHashMap<>();
    private final ServerWorld serverWorld;

    public ServerGraphWorldStorage(ServerWorld world, Path dataDir, boolean syncChunkWrites) {
        this.serverWorld = world;

        for (GraphUniverseImpl universe : GraphLibPlatform.INSTANCE.getUniverseRegistry()) {
            Identifier universeId = universe.getId();
            Path path = dataDir.resolve(universeId.getNamespace()).resolve(universeId.getPath());

            worlds.put(universeId, universe.createGraphWorld(world, path, syncChunkWrites));
        }
    }

    @Override
    public @NotNull ServerGraphWorldImpl get(@NotNull Identifier universe) {
        if (!worlds.containsKey(universe)) {
            throw new IllegalStateException(
                "Attempted to get a graph world for a universe that has not been registered. Make sure to call the universe's register() function in your mod's init. Universe: " +
                    universe);
        }

        return worlds.get(universe);
    }

    @Override
    public @NotNull Map<Identifier, ServerGraphWorldImpl> getAll() {
        return worlds;
    }

    public void onWorldChunkLoad(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            // Prevent worlds from interfering with each other
            try {
                world.onWorldChunkLoad(pos);
            } catch (Exception e) {
                GLLog.error("Error loading chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void onWorldChunkUnload(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.onWorldChunkUnload(pos);
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void tick() {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.tick();
            } catch (Exception e) {
                GLLog.error("Error ticking GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), e);
            }
        }
    }

    public void saveChunk(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.saveChunk(pos);
            } catch (Exception e) {
                GLLog.error("Error saving chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void saveAll(boolean flush) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.saveAll(flush);
            } catch (Exception e) {
                GLLog.error("Error saving all chunks in GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), e);
            }
        }
    }

    @Override
    public void close() {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.close();
            } catch (Exception e) {
                GLLog.error("Error closing GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), e);
            }
        }
    }

    public void sendChunkDataPackets(ServerPlayerEntity player, ChunkPos pos) {

    }
}
