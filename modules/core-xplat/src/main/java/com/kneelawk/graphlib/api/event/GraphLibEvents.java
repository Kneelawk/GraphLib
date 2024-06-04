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

package com.kneelawk.graphlib.api.event;

import net.minecraft.server.level.ServerLevel;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;

/**
 * This contains the events fired for different graph operations.
 */
public final class GraphLibEvents {
    private GraphLibEvents() {
    }

    /**
     * This event is fired when a graph is created in any world.
     */
    public static final Event<GraphCreatedListener> GRAPH_CREATED = Event.createSimple(GraphCreatedListener.class);

    /**
     * This event is fired when a graph is changed in any world.
     */
    public static final Event<GraphUpdatedListener> GRAPH_UPDATED = Event.createSimple(GraphUpdatedListener.class);

    /**
     * This event is fired when a graph is about to be unloaded.
     * <p>
     * Note: Unloading cannot be cancelled.
     */
    public static final Event<GraphUnloadingListener> GRAPH_UNLOADING =
        Event.createSimple(GraphUnloadingListener.class);

    /**
     * This event is fired when a graph is destroyed in any world.
     */
    public static final Event<GraphDestroyedListener> GRAPH_DESTROYED =
        Event.createSimple(GraphDestroyedListener.class);

    /**
     * Listener for when a graph is created in any world.
     */
    public interface GraphCreatedListener {
        /**
         * Called when a graph is created in any world.
         *
         * @param world      the world in which the graph was created.
         * @param graphWorld the graph-world in which the graph was created.
         * @param graph      the graph that was created.
         */
        void graphCreated(ServerLevel world, GraphWorld graphWorld, BlockGraph graph);
    }

    /**
     * Listener for when a graph is changed in any world.
     */
    public interface GraphUpdatedListener {
        /**
         * Called when a graph is changed in any world.
         *
         * @param world      the world in which the graph was changed.
         * @param graphWorld the graph-world in which the graph was changed.
         * @param graph      the graph that was changed.
         */
        void graphUpdated(ServerLevel world, GraphWorld graphWorld, BlockGraph graph);
    }

    /**
     * Listener for when a graph is about to be unloaded.
     * <p>
     * Note: Unloading cannot be cancelled.
     */
    public interface GraphUnloadingListener {
        /**
         * Called when a graph is about to be unloaded in any world.
         * <p>
         * Note: Unloading cannot be cancelled.
         *
         * @param world      the world in which the graph is being unloaded.
         * @param graphWorld the graph-world in which the graph is being unloaded.
         * @param graph      the graph that is being unloaded.
         */
        void graphUnloading(ServerLevel world, GraphWorld graphWorld, BlockGraph graph);
    }

    /**
     * Listener for when a graph is destroyed in any world.
     */
    public interface GraphDestroyedListener {
        /**
         * Called when a graph is destroyed in any world.
         *
         * @param world      the world in which the graph was destroyed.
         * @param graphWorld the graph-world in which the graph was destroyed.
         * @param id         the id of the graph that was destroyed.
         */
        void graphDestroyed(ServerLevel world, GraphWorld graphWorld, long id);
    }
}
