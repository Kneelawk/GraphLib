package com.kneelawk.wirenetlib.mixin.impl;

import com.kneelawk.wirenetlib.Constants;
import com.kneelawk.wirenetlib.WireNetLibMod;
import com.kneelawk.wirenetlib.mixin.api.WireNetworkControllerAccess;
import com.kneelawk.wirenetlib.wire.WireNetworkController;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements WireNetworkControllerAccess {
    @Unique
    private WireNetworkController controller;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onCreate(
            ServerWorld serverWorld,
            LevelStorage.Session session,
            DataFixer dataFixer,
            StructureManager structureManager,
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
        controller = new WireNetworkController(serverWorld,
                session.getWorldDirectory(serverWorld.getRegistryKey()).resolve(Constants.DATA_DIRNAME)
                        .resolve(Constants.MOD_ID).resolve(Constants.WIRENET_DIRNAME), syncChunkWrites);
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        try {
            controller.close();
        } catch (Exception e) {
            WireNetLibMod.log.error("Error saving wire network controller.", e);
        }
    }

    @Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At("HEAD"))
    public void onSave(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
        controller.saveChunk(chunk.getPos());
    }

    @Override
    public WireNetworkController wirenetlib_getWireNetworkController() {
        return controller;
    }
}
