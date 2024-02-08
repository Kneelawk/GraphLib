/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
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

package com.kneelawk.graphlib.debugrender.fabric.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import com.mojang.blaze3d.vertex.BufferBuilder;

import com.kneelawk.graphlib.debugrender.impl.client.GLClientDebugNet;
import com.kneelawk.graphlib.debugrender.impl.client.GraphLibDebugRenderClientImpl;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.DebugRenderer;
import com.kneelawk.kmodlib.client.overlay.RenderToOverlay;

@SuppressWarnings("unused")
public class GraphLibDebugRenderFabricModClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        GraphLibDebugRenderClientImpl.register();

        GLClientDebugNet.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.clear();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.clear();
        });

        ClientChunkEvents.CHUNK_LOAD.register(
            (world, chunk) -> GraphLibDebugRenderClientImpl.chunkLoad(chunk.getPos().toLong()));
        ClientChunkEvents.CHUNK_UNLOAD.register(
            (world, chunk) -> GraphLibDebugRenderClientImpl.chunkUnload(chunk.getPos().toLong()));

        // RenderToOverlay stuff
        RenderToOverlay.LAYER_MAP.put(DebugRenderer.Layers.DEBUG_LINES,
            new BufferBuilder(DebugRenderer.Layers.DEBUG_LINES.getExpectedBufferSize()));
        RenderToOverlay.LAYER_MAP.put(DebugRenderer.Layers.DEBUG_QUADS,
            new BufferBuilder(DebugRenderer.Layers.DEBUG_QUADS.getExpectedBufferSize()));
        RenderToOverlay.EVENT.register(
            ctx -> DebugRenderer.render(ctx.matrixStack(), ctx.camera().getPos(), ctx.consumers()));
    }
}
