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

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.kneelawk.graphlib.debugrender.impl.GLDebugNet;
import com.kneelawk.graphlib.debugrender.impl.GraphLibDebugRenderImpl;
import com.kneelawk.graphlib.debugrender.impl.command.GraphLibDebugRenderCommand;
import com.kneelawk.graphlib.neoforge.api.event.GraphCreatedEvent;
import com.kneelawk.graphlib.neoforge.api.event.GraphDestroyedEvent;
import com.kneelawk.graphlib.neoforge.api.event.GraphUpdatedEvent;
import com.kneelawk.graphlib.neoforge.impl.event.AddUniverseSubcommandsEvent;

@Mod(GraphLibDebugRenderImpl.MOD_ID)
@Mod.EventBusSubscriber
public class GraphLibDebugRenderNeoforgeMod {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        GLDebugNet.onServerStart();
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        GLDebugNet.onServerStop();
    }

    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        GLDebugNet.onDisconnect(event.getEntity().getUuid());
    }

    @SubscribeEvent
    public static void onGraphCreated(GraphCreatedEvent event) {
        GLDebugNet.onGraphCreated(event.getWorld(), event.getGraphWorld(), event.getGraph());
    }

    @SubscribeEvent
    public static void onGraphUpdated(GraphUpdatedEvent event) {
        GLDebugNet.onGraphUpdated(event.getWorld(), event.getGraphWorld(), event.getGraph());
    }

    @SubscribeEvent
    public static void onGraphDestroyed(GraphDestroyedEvent event) {
        GLDebugNet.onGraphDestroyed(event.getWorld(), event.getGraphWorld(), event.getGraphId());
    }

    @SubscribeEvent
    public static void onAddUniverseSubcommands(AddUniverseSubcommandsEvent event) {
        GraphLibDebugRenderCommand.addUniverseSubcommands(event.universe);
    }
}
