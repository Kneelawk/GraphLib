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

package com.kneelawk.graphlib.debugrender.impl;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;

public class GraphLibDebugRenderImpl {
    public static final String MOD_ID = "graphlib_debugrender";

    public static final Map<ResourceLocation, Map<ResourceLocation, BlockNodeDebugPacketEncoder>> DEBUG_ENCODERS =
        new HashMap<>();

    public static @Nullable BlockNodeDebugPacketEncoder getDebugEncoder(ResourceLocation universeId,
                                                                        ResourceLocation typeId) {
        Map<ResourceLocation, BlockNodeDebugPacketEncoder> universeDecoders = DEBUG_ENCODERS.get(universeId);
        if (universeDecoders == null) return null;
        return universeDecoders.get(typeId);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
