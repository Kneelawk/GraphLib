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

package com.kneelawk.graphlib.debugrender.neoforge.impl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.kneelawk.graphlib.debugrender.impl.GLDebugNet;
import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;
import com.kneelawk.graphlib.debugrender.impl.client.GLClientDebugNet;
import com.kneelawk.graphlib.debugrender.impl.payload.DebuggingStopPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphDestroyPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdateBulkPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdatePayload;

@Mod(GraphLibDebugRenderImpl.MOD_ID)
@EventBusSubscriber
public class GraphLibDebugRenderNeoforgeMod {
    public GraphLibDebugRenderNeoforgeMod(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloads);
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(GraphLibDebugRenderImpl.MOD_ID);
        registrar.playToClient(GraphUpdatePayload.ID, GraphUpdatePayload.CODEC,
            (payload, ctx) -> GLClientDebugNet.onGraphUpdate(payload, ctx::enqueueWork));
        registrar.playToClient(GraphUpdateBulkPayload.ID, GraphUpdateBulkPayload.CODEC,
            (payload, ctx) -> GLClientDebugNet.onGraphUpdateBulk(payload, ctx::enqueueWork));
        registrar.playToClient(GraphDestroyPayload.ID, GraphDestroyPayload.CODEC,
            (payload, ctx) -> GLClientDebugNet.onGraphDestroy(payload, ctx::enqueueWork));
        registrar.playToClient(DebuggingStopPayload.ID, DebuggingStopPayload.CODEC,
            (payload, ctx) -> GLClientDebugNet.onDebugginStop(payload, ctx::enqueueWork));
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        GLDebugNet.onServerStart();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        GLDebugNet.onServerStop();
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        GLDebugNet.onDisconnect(event.getEntity().getUUID());
    }
}
