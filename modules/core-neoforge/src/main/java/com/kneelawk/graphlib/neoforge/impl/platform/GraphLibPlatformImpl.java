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

package com.kneelawk.graphlib.neoforge.impl.platform;

import net.neoforged.neoforge.common.NeoForge;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;
import com.kneelawk.graphlib.neoforge.api.event.GraphCreatedEvent;
import com.kneelawk.graphlib.neoforge.api.event.GraphDestroyedEvent;
import com.kneelawk.graphlib.neoforge.api.event.GraphUnloadingEvent;
import com.kneelawk.graphlib.neoforge.api.event.GraphUpdatedEvent;
import com.kneelawk.graphlib.neoforge.impl.GraphLibNeoForgeMod;
import com.kneelawk.graphlib.neoforge.impl.event.AddUniverseSubcommandsEvent;

public class GraphLibPlatformImpl implements GraphLibPlatform {
    @Override
    public void fireAddUniverseSubcommands(RequiredArgumentBuilder<ServerCommandSource, Identifier> universe) {
        NeoForge.EVENT_BUS.post(new AddUniverseSubcommandsEvent(universe));
    }

    @Override
    public void fireGraphCreated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        NeoForge.EVENT_BUS.post(new GraphCreatedEvent(world, graphWorld, graph));
    }

    @Override
    public void fireGraphUpdated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        NeoForge.EVENT_BUS.post(new GraphUpdatedEvent(world, graphWorld, graph));
    }

    @Override
    public void fireGraphUnloading(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        NeoForge.EVENT_BUS.post(new GraphUnloadingEvent(world, graphWorld, graph));
    }

    @Override
    public void fireGraphDestroyed(ServerWorld world, GraphWorld graphWorld, long id) {
        NeoForge.EVENT_BUS.post(new GraphDestroyedEvent(world, graphWorld, id));
    }
}
