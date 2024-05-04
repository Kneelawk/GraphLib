package com.kneelawk.graphlib.impl.graph;

import java.nio.file.Path;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class ServerGraphWorldStorage implements GraphWorldStorage, AutoCloseable {
    private final Map<ResourceLocation, ServerGraphWorldImpl> worlds = new Object2ObjectLinkedOpenHashMap<>();
    private final ServerLevel serverWorld;

    public ServerGraphWorldStorage(LevelStorageSource.LevelStorageAccess session, ServerLevel world, Path dataDir, boolean syncChunkWrites) {
        this.serverWorld = world;

        for (GraphUniverseImpl universe : GraphLibImpl.UNIVERSE.values()) {
            ResourceLocation universeId = universe.getId();
            Path path = dataDir.resolve(universeId.getNamespace()).resolve(universeId.getPath());

            worlds.put(universeId, universe.createGraphWorld(session, world, path, syncChunkWrites));
        }
    }

    @Override
    public @NotNull ServerGraphWorldImpl get(@NotNull ResourceLocation universe) {
        if (!worlds.containsKey(universe)) {
            throw new IllegalStateException(
                "Attempted to get a graph world for a universe that has not been registered. Make sure to call the universe's register() function in your mod's init. Universe: " +
                    universe);
        }

        return worlds.get(universe);
    }

    @Override
    public @NotNull Map<ResourceLocation, ServerGraphWorldImpl> getAll() {
        return worlds;
    }

    public void onWorldChunkLoad(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            // Prevent worlds from interfering with each other
            try {
                world.onWorldChunkLoad(pos);
            } catch (Exception e) {
                GLLog.error("Error loading chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.dimension().location(), pos, e);
            }
        }
    }

    public void onWorldChunkUnload(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.onWorldChunkUnload(pos);
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.dimension().location(), pos, e);
            }
        }
    }

    public void tick() {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.tick();
            } catch (Exception e) {
                GLLog.error("Error ticking GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.dimension().location(), e);
            }
        }
    }

    public void saveChunk(ChunkPos pos) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.saveChunk(pos);
            } catch (Exception e) {
                GLLog.error("Error saving chunk in GraphWorld. World: '{}'/{}, Chunk: {}", serverWorld,
                    serverWorld.dimension().location(), pos, e);
            }
        }
    }

    public void saveAll(boolean flush) {
        for (ServerGraphWorldImpl world : worlds.values()) {
            try {
                world.saveAll(flush);
            } catch (Exception e) {
                GLLog.error("Error saving all chunks in GraphWorld. World: '{}'/{}", serverWorld,
                    serverWorld.dimension().location(), e);
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
                    serverWorld.dimension().location(), e);
            }
        }
    }

    public void sendChunkDataPackets(ServerPlayer player, ChunkPos pos) {

    }
}
