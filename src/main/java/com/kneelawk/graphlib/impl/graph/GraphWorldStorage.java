package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class GraphWorldStorage implements AutoCloseable {
    private final Map<Identifier, GraphWorldImpl> worlds = new Object2ObjectLinkedOpenHashMap<>();
    private final ServerWorld serverWorld;

    public GraphWorldStorage(ServerWorld world, Path dataDir, boolean syncChunkWrites) {
        this.serverWorld = world;

        for (GraphUniverseImpl universe : GraphLibImpl.UNIVERSE) {
            Identifier universeId = universe.getId();
            Path path = dataDir.resolve(universeId.getNamespace()).resolve(universeId.getPath());

            worlds.put(universeId, universe.createGraphWorld(world, path, syncChunkWrites));
        }
    }

    public GraphWorldImpl get(Identifier universe) {
        if (!worlds.containsKey(universe)) {
            throw new IllegalStateException(
                "Attempted to get a graph world for a universe that has not been registered. Make sure to call the universe's register() function in your mod's init. Universe: " +
                    universe);
        }

        return worlds.get(universe);
    }

    public void onWorldChunkLoad(ChunkPos pos) {
        for (GraphWorldImpl world : worlds.values()) {
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
        for (GraphWorldImpl world : worlds.values()) {
            try {
                world.onWorldChunkUnload(pos);
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void tick() {
        for (GraphWorldImpl world : worlds.values()) {
            try {
                world.tick();
            } catch (Exception e) {
                GLLog.error("Error ticking GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), e);
            }
        }
    }

    public void saveChunk(ChunkPos pos) {
        for (GraphWorldImpl world : worlds.values()) {
            try {
                world.saveChunk(pos);
            } catch (Exception e) {
                GLLog.error("Error saving chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.getRegistryKey().getValue(), pos, e);
            }
        }
    }

    public void saveAll(boolean flush) {
        for (GraphWorldImpl world : worlds.values()) {
            world.saveAll(flush);
        }
    }

    @Override
    public void close() throws Exception {
        for (GraphWorldImpl world : worlds.values()) {
            world.close();
        }
    }
}
