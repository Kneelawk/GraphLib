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

package com.kneelawk.graphlib.syncing.impl.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;
import com.kneelawk.graphlib.syncing.impl.CommonProxy;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldStorage;
import com.kneelawk.graphlib.syncing.impl.mixin.api.ClientStorageHelper;

@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {
    static void init() {
        INSTANCE = new ClientProxy();
    }

    @Override
    public @Nullable World getClientWorld() {
        return MinecraftClient.getInstance().world;
    }

    @Override
    @Deprecated
    public @NotNull GraphWorldStorage getStorage(@NotNull World world) {
        if (world instanceof ServerWorld serverWorld) {
            return StorageHelper.getStorage(serverWorld);
        }
        if (world instanceof ClientWorld clientWorld) {
            return ClientStorageHelper.getStorage(clientWorld);
        }

        throw new IllegalArgumentException(
            "PHYSICAL CLIENT: World must be a ClientWorld or a ServerWorld, but was: " + world.getClass());
    }

    @Override
    public @Nullable GraphWorldStorage getSidedStorage(@NotNull World world) {
        // Turns out it is more common than you might think for a world to be neither a ServerWorld nor a ClientWorld.
        if (world instanceof ServerWorld serverWorld) {
            return StorageHelper.getStorage(serverWorld);
        }
        if (world instanceof ClientWorld clientWorld) {
            return ClientStorageHelper.getStorage(clientWorld);
        }

        return null;
    }

    @Override
    public @Nullable ClientGraphWorldStorage getClientStorage() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            GLLog.warn("Attempted to get client storage before the client had loaded a world.",
                new RuntimeException("Stack Trace"));
            return null;
        }

        return ClientStorageHelper.getStorage(world);
    }
}
