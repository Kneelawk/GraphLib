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

package com.kneelawk.graphlib.debugrender.neoforge.impl.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;
import com.kneelawk.graphlib.debugrender.impl.client.GLClientDebugNet;
import com.kneelawk.graphlib.debugrender.impl.payload.DebuggingStopPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphDestroyPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdateBulkPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdatePayload;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GLDRPayloadClient {
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(GraphLibDebugRenderImpl.MOD_ID);
        registrar.play(GraphUpdatePayload.ID, GraphUpdatePayload::decode, handler -> handler.client(
            (payload, ctx) -> GLClientDebugNet.onGraphUpdate(payload, ctx.workHandler()::execute)));
        registrar.play(GraphUpdateBulkPayload.ID, GraphUpdateBulkPayload::decode, handler -> handler.client(
            (payload, ctx) -> GLClientDebugNet.onGraphUpdateBulk(payload, ctx.workHandler()::execute)));
        registrar.play(GraphDestroyPayload.ID, GraphDestroyPayload::new, handler -> handler.client(
            (payload, ctx) -> GLClientDebugNet.onGraphDestroy(payload, ctx.workHandler()::execute)));
        registrar.play(DebuggingStopPayload.ID, DebuggingStopPayload::new, handler -> handler.client(
            (payload, ctx) -> GLClientDebugNet.onDebugginStop(payload, ctx.workHandler()::execute)));
    }
}
