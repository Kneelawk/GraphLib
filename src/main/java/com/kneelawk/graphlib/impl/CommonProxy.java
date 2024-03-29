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

package com.kneelawk.graphlib.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import com.kneelawk.graphlib.impl.graph.ClientGraphWorldStorage;
import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

public class CommonProxy {
    public static CommonProxy INSTANCE = new CommonProxy();

    public @Nullable World getClientWorld() {
        return null;
    }

    @Deprecated
    public @NotNull GraphWorldStorage getStorage(@NotNull World world) {
        if (world instanceof ServerWorld serverWorld) {
            return StorageHelper.getStorage(serverWorld);
        }

        throw new IllegalArgumentException(
            "PHYSICAL SERVER: World must be a ServerWorld, but was: " + world.getClass());
    }

    public @Nullable ClientGraphWorldStorage getClientStorage() {
        return null;
    }

    public @Nullable GraphWorldStorage getSidedStorage(@NotNull World world) {
        if (world instanceof ServerWorld serverWorld) {
            return StorageHelper.getStorage(serverWorld);
        }

        return null;
    }
}
