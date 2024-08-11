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

package com.kneelawk.graphlib.debugrender.fabric.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import com.kneelawk.graphlib.debugrender.impl.GLDebugNet;
import com.kneelawk.graphlib.debugrender.impl.payload.DebuggingStopPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphDestroyPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdateBulkPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdatePayload;
import com.kneelawk.graphlib.impl.GLLog;

public class GraphLibDebugRenderFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        GLLog.info("Initializing GraphLib Debug Render...");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> GLDebugNet.onServerStart());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> GLDebugNet.onServerStop());
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> GLDebugNet.onDisconnect(handler.getPlayer().getUUID()));

        // register packet ids on both server and client
        PayloadTypeRegistry.playS2C().register(GraphUpdatePayload.ID, GraphUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GraphUpdateBulkPayload.ID, GraphUpdateBulkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GraphDestroyPayload.ID, GraphDestroyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DebuggingStopPayload.ID, DebuggingStopPayload.CODEC);

        GLLog.info("GraphLib Debug Render initialized.");
    }
}
