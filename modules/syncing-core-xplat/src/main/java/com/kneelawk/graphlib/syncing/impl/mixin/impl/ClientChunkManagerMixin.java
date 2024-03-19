/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
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

package com.kneelawk.graphlib.syncing.impl.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldStorage;
import com.kneelawk.graphlib.syncing.impl.mixin.api.ClientGraphWorldStorageAccess;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin implements ClientGraphWorldStorageAccess {
    @Unique
    private ClientGraphWorldStorage storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ClientWorld world, int loadDistance, CallbackInfo ci) {
        storage = new ClientGraphWorldStorage(world, loadDistance);
    }

    @Inject(method = "unload", at = @At("RETURN"))
    private void onUnload(ChunkPos pos, CallbackInfo ci) {
        storage.unload(pos);
    }

    @Inject(method = "setChunkMapCenter", at = @At("RETURN"))
    private void onSetChunkMapCenter(int x, int z, CallbackInfo ci) {
        storage.setChunkMapCenter(x, z);
    }

    @Inject(method = "updateLoadDistance", at = @At("RETURN"))
    private void onUpdateLoadDistance(int loadDistance, CallbackInfo ci) {
        storage.updateLoadDistance(loadDistance);
    }

    @Override
    public ClientGraphWorldStorage graphlib_syncing_getClientGraphWorldStorage() {
        return storage;
    }
}
