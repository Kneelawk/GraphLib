package com.kneelawk.graphlib.impl.mixin.impl;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixer;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.GraphWorldStorageAccess;

@Mixin(ChunkMap.class)
public class ThreadedChunkManagerMixin implements GraphWorldStorageAccess {
    @Shadow
    @Final
    ServerLevel level;

    @Unique
    private ServerGraphWorldStorage storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCreate(ServerLevel serverLevel, LevelStorageSource.LevelStorageAccess levelStorageAccess,
                          DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor,
                          BlockableEventLoop blockableEventLoop, LightChunkGetter lightChunkGetter,
                          ChunkGenerator chunkGenerator, ChunkProgressListener chunkProgressListener,
                          ChunkStatusUpdateListener chunkStatusUpdateListener, Supplier supplier,
                          TicketStorage ticketStorage, int i, boolean bl, CallbackInfo ci) {
        storage = new ServerGraphWorldStorage(levelStorageAccess, serverLevel,
            levelStorageAccess.getDimensionPath(serverLevel.dimension()).resolve(Constants.DATA_DIRNAME), bl);
    }

    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void onSaveAllChunks(boolean flush, CallbackInfo ci) {
        try {
            storage.saveAll(flush);
        } catch (Exception e) {
            GLLog.error("Error saving graph world storage. World: '{}'/{}", level, level.dimension().location(),
                e);
        }
    }

    @Override
    public @NotNull ServerGraphWorldStorage graphlib_getGraphWorldStorage() {
        return storage;
    }
}
