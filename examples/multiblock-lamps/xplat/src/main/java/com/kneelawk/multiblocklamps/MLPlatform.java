/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.multiblocklamps;

import java.util.ServiceLoader;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

public interface MLPlatform {
    MLPlatform INSTANCE = ServiceLoader.load(MLPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Failed to load Multiblock Lamps platform"));

    <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                        MapCodec<? extends Block> codec);

    Supplier<GraphUniverse> registerUniverse(String path, Supplier<GraphUniverse> creator);
}
