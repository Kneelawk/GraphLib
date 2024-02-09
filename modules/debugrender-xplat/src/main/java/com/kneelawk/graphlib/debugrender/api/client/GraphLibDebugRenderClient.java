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

package com.kneelawk.graphlib.debugrender.api.client;

import java.util.HashMap;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.debugrender.api.GraphLibDebugRender;
import com.kneelawk.graphlib.debugrender.api.client.render.BlockNodeDebugRenderer;
import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.GraphLibDebugRenderClientImpl;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.BlockNodeDebugRendererHolder;

/**
 * Graph Lib Client-Side Public API. This class contains static methods and fields for registering
 * {@link BlockNodeDebugPacketDecoder}s and renderers.
 */
public final class GraphLibDebugRenderClient {
    private GraphLibDebugRenderClient() {
    }

    /**
     * Registers a {@link BlockNodeDebugPacketDecoder} in the given universe for the given block node type id.
     * <p>
     * Only register a decoder if you also register an encoder with
     * {@link GraphLibDebugRender#registerDebugEncoder(Identifier, Identifier, BlockNodeDebugPacketEncoder)}
     * to provide custom node data to the client.
     *
     * @param universeId the universe this decoder is to be registered under.
     * @param typeId     the block node type id this decoder is to be registered for.
     * @param decoder    the decoder.
     */
    public static void registerDebugDecoder(Identifier universeId, Identifier typeId,
                                            BlockNodeDebugPacketDecoder decoder) {
        GraphLibDebugRenderClientImpl.DEBUG_DECODERS.computeIfAbsent(universeId, _id -> new HashMap<>())
            .put(typeId, decoder);
    }

    /**
     * Registers a {@link BlockNodeDebugRenderer} in the given universe for the given block node render id.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     *
     * @param universeId the universe this renderer is registered under.
     * @param renderId   the render id this renderer is to be registered for.
     * @param clazz      the class of the {@link DebugBlockNode} that this renderer expects.
     * @param renderer   the renderer.
     * @param <N>        the type of the {@link DebugBlockNode} that this renderer expects.
     */
    public static <N extends DebugBlockNode> void registerDebugRenderer(Identifier universeId, Identifier renderId,
                                                                        Class<N> clazz,
                                                                        BlockNodeDebugRenderer<N> renderer) {
        GraphLibDebugRenderClientImpl.DEBUG_RENDERERS.computeIfAbsent(universeId, _id -> new HashMap<>())
            .put(renderId, new BlockNodeDebugRendererHolder<>(clazz, renderer));
    }

    /**
     * Registers a {@link BlockNodeDebugRenderer} for the given block node render id for all universes.
     * <p>
     * Only register a renderer if you need custom rendering for your nodes. GraphLib has default renderers that fill
     * most use cases.
     * <p>
     * Renderers for specific universes take priority over this. It is advised to only register for the universe(s) you
     * expect your renderer to be used in.
     *
     * @param renderId the render id this renderer is to be registered for.
     * @param clazz    the class of the {@link DebugBlockNode} that this renderer expects.
     * @param renderer the renderer.
     * @param <N>      the type of the {@link DebugBlockNode} that this renderer expects.
     */
    public static <N extends DebugBlockNode> void registerDebugRendererForAllUniverses(Identifier renderId,
                                                                                       Class<N> clazz,
                                                                                       BlockNodeDebugRenderer<N> renderer) {
        GraphLibDebugRenderClientImpl.ALL_UNIVERSE_DEBUG_RENDERERS.put(renderId,
            new BlockNodeDebugRendererHolder<>(clazz, renderer));
    }
}
