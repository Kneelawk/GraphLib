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

package com.kneelawk.graphlib.neoforge.api.event;

import net.neoforged.bus.api.Event;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.graph.GraphWorld;

/**
 * Fired after a graph has been destroyed.
 */
public class GraphDestroyedEvent extends Event {
    private final ServerWorld world;
    private final GraphWorld graphWorld;
    private final long graphId;

    /**
     * Constructs a new graph-destroyed event.
     *
     * @param world      the block world.
     * @param graphWorld the graph world.
     * @param graphId    the id of the graph that was destroyed.
     */
    public GraphDestroyedEvent(ServerWorld world, GraphWorld graphWorld, long graphId) {
        this.world = world;
        this.graphWorld = graphWorld;
        this.graphId = graphId;
    }

    /**
     * Gets the block world.
     *
     * @return the block world.
     */
    public ServerWorld getWorld() {
        return world;
    }

    /**
     * Gets the graph world.
     *
     * @return the graph world.
     */
    public GraphWorld getGraphWorld() {
        return graphWorld;
    }

    /**
     * Gets the id of the graph that was destroyed.
     *
     * @return the id of the graph that was destroyed.
     */
    public long getGraphId() {
        return graphId;
    }
}
