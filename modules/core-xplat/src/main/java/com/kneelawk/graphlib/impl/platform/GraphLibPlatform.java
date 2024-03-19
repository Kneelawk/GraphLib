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

package com.kneelawk.graphlib.impl.platform;

import java.util.ServiceLoader;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public interface GraphLibPlatform {
    GraphLibPlatform INSTANCE = ServiceLoader.load(GraphLibPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Failed to load platform implementation"));

    void fireAddUniverseSubcommands(RequiredArgumentBuilder<ServerCommandSource, Identifier> universe);

    void fireGraphCreated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph);

    void fireGraphUpdated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph);

    void fireGraphUnloading(ServerWorld world, GraphWorld graphWorld, BlockGraph graph);

    void fireGraphDestroyed(ServerWorld world, GraphWorld graphWorld, long id);
}
