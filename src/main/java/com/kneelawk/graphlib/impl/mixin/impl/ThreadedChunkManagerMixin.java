package com.kneelawk.graphlib.impl.mixin.impl;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixer;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedChunkManager;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.storage.WorldSaveStorage;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.GraphWorldStorageAccess;

@Mixin(ThreadedChunkManager.class)
public class ThreadedChunkManagerMixin implements GraphWorldStorageAccess {
    @Shadow
    @Final
    ServerWorld world;

    @Unique
    private ServerGraphWorldStorage storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCreate(
        ServerWorld world,
        WorldSaveStorage.Session session,
        DataFixer dataFixer,
        StructureTemplateManager structureTemplateManager,
        Executor executor,
        ThreadExecutor<Runnable> threadExecutor,
        ChunkProvider chunkProvider,
        ChunkGenerator chunkGenerator,
        WorldGenerationProgressListener worldGenerationProgressListener,
        ChunkStatusChangeListener chunkStatusChangeListener,
        Supplier<PersistentStateManager> supplier,
        int i,
        boolean syncChunkWrites,
        CallbackInfo ci
    ) {
        storage = new ServerGraphWorldStorage(world,
            session.getWorldDirectory(world.getRegistryKey()).resolve(Constants.DATA_DIRNAME), syncChunkWrites);
    }

    @Inject(method = "save(Z)V", at = @At("HEAD"))
    private void onSave(boolean flush, CallbackInfo ci) {
        try {
            storage.saveAll(flush);
        } catch (Exception e) {
            GLLog.error("Error saving graph world storage. World: '{}'/{}", world, world.getRegistryKey().getValue(),
                e);
        }
    }

    @Inject(method = "sendChunkDataPackets", at = @At("RETURN"))
    private void onSendChunkDataPackets(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> mutableObject,
                                        WorldChunk chunk, CallbackInfo ci) {
        storage.sendChunkDataPackets(player, chunk.getPos());
    }

    @Override
    public @NotNull ServerGraphWorldStorage graphlib_getGraphWorldStorage() {
        return storage;
    }
}
