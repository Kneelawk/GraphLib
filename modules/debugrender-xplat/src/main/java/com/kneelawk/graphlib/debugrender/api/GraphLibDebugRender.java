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

package com.kneelawk.graphlib.debugrender.api;

import java.util.HashMap;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;
import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;

/**
 * Graph Lib Debug Render public API. This class contains methods for interacting with GraphLib's debug renderer.
 */
public class GraphLibDebugRender {
    private GraphLibDebugRender() {}

    /**
     * Registers a {@link BlockNodeDebugPacketEncoder} in the given universe for the given block node type id.
     *
     * @param universeId the id of the universe this is being registered for.
     * @param typeId     the type id of the block node this encoder should be used to encode.
     * @param encoder    the encoder being registered.
     */
    public static void registerDebugEncoder(Identifier universeId, Identifier typeId,
                                            BlockNodeDebugPacketEncoder encoder) {
        GraphLibDebugRenderImpl.DEBUG_ENCODERS.computeIfAbsent(universeId, _id -> new HashMap<>()).put(typeId, encoder);
    }
}
