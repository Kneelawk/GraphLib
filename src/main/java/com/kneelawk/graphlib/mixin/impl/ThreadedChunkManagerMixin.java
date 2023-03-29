package com.kneelawk.graphlib.mixin.impl;

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

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedChunkManager;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.storage.WorldSaveStorage;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.GLLog;
import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import com.kneelawk.graphlib.mixin.api.BlockGraphControllerAccess;

@Mixin(ThreadedChunkManager.class)
public class ThreadedChunkManagerMixin implements BlockGraphControllerAccess {
    @Shadow
    @Final
    ServerWorld world;

    @Unique
    private SimpleBlockGraphController controller;

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
        controller = new SimpleBlockGraphController(world,
            session.getWorldDirectory(world.getRegistryKey()).resolve(Constants.DATA_DIRNAME).resolve(Constants.MOD_ID)
                .resolve(Constants.GRAPHDATA_DIRNAME), syncChunkWrites);
    }

    @Inject(method = "save(Z)V", at = @At("HEAD"))
    private void onSave(boolean flush, CallbackInfo ci) {
        try {
            controller.saveAll();
        } catch (Exception e) {
            GLLog.error("Error saving graph controller. World: '{}'/{}", world, world.getRegistryKey().getValue(), e);
        }
    }

    @Override
    public @NotNull SimpleBlockGraphController graphlib_getGraphController() {
        return controller;
    }
}
