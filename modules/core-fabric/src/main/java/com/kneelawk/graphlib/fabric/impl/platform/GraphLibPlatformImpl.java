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

package com.kneelawk.graphlib.fabric.impl.platform;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.fabric.api.event.GraphLibEvents;
import com.kneelawk.graphlib.fabric.impl.GraphLibFabricMod;
import com.kneelawk.graphlib.fabric.impl.event.InternalEvents;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;

public class GraphLibPlatformImpl implements GraphLibPlatform {
    @Override
    public Registry<GraphUniverseImpl> getUniverseRegistry() {
        return GraphLibFabricMod.UNIVERSE;
    }

    @Override
    public void fireAddUniverseSubcommands(RequiredArgumentBuilder<ServerCommandSource, Identifier> universe) {
        InternalEvents.ADD_UNIVERSE_SUBCOMMANDS.invoker().addUniverseSubcommands(universe);
    }

    @Override
    public void fireGraphCreated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        GraphLibEvents.GRAPH_CREATED.invoker().graphCreated(world, graphWorld, graph);
    }

    @Override
    public void fireGraphUpdated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        GraphLibEvents.GRAPH_UPDATED.invoker().graphUpdated(world, graphWorld, graph);
    }

    @Override
    public void fireGraphUnloading(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        GraphLibEvents.GRAPH_UNLOADING.invoker().graphUnloading(world, graphWorld, graph);
    }

    @Override
    public void fireGraphDestroyed(ServerWorld world, GraphWorld graphWorld, long id) {
        GraphLibEvents.GRAPH_DESTROYED.invoker().graphDestroyed(world, graphWorld, id);
    }
}
