package com.kneelawk.graphlib.api.v1;

import com.kneelawk.graphlib.api.v1.graph.BlockGraph;
import com.kneelawk.graphlib.api.v1.graph.GraphWorld;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

/**
 * This contains the events fired for different graph operations.
 */
public final class GraphLibEvents {
    private GraphLibEvents() {
    }

    /**
     * This event is fired when a graph is created in any world.
     */
    public static final Event<GraphCreatedListener> GRAPH_CREATED =
        EventFactory.createArrayBacked(GraphCreatedListener.class, (world, controller, graph) -> {
        }, listeners -> (world, controller, graph) -> {
            for (GraphCreatedListener listener : listeners) {
                listener.graphCreated(world, controller, graph);
            }
        });

    /**
     * This event is fired when a graph is changed in any world.
     */
    public static final Event<GraphUpdatedListener> GRAPH_UPDATED =
        EventFactory.createArrayBacked(GraphUpdatedListener.class, (world, controller, graph) -> {
        }, listeners -> (world, controller, graph) -> {
            for (GraphUpdatedListener listener : listeners) {
                listener.graphUpdated(world, controller, graph);
            }
        });

    /**
     * This event is fired when a graph is destroyed in any world.
     */
    public static final Event<GraphDestroyedListener> GRAPH_DESTROYED =
        EventFactory.createArrayBacked(GraphDestroyedListener.class, (world, controller, id) -> {
        }, listeners -> (world, controller, id) -> {
            for (GraphDestroyedListener listener : listeners) {
                listener.graphDestroyed(world, controller, id);
            }
        });

    /**
     * Listener for when a graph is created in any world.
     */
    public interface GraphCreatedListener {
        /**
         * Called when a graph is created in any world.
         *
         * @param world      the world in which the graph was created.
         * @param controller the graph-controller in which the graph was created.
         * @param graph      the graph that was created.
         */
        void graphCreated(ServerWorld world, GraphWorld controller, BlockGraph graph);
    }

    /**
     * Listener for when a graph is changed in any world.
     */
    public interface GraphUpdatedListener {
        /**
         * Called when a graph is changed in any world.
         *
         * @param world      the world in which the graph was changed.
         * @param controller the graph-controller in which the graph was changed.
         * @param graph      the graph that was changed.
         */
        void graphUpdated(ServerWorld world, GraphWorld controller, BlockGraph graph);
    }

    /**
     * Listener for when a graph is destroyed in any world.
     */
    public interface GraphDestroyedListener {
        /**
         * Called when a graph is destroyed in any world.
         *
         * @param world      the world in which the graph was destroyed.
         * @param controller the graph-controller in which the graph was destroyed.
         * @param id         the id of the graph that was destroyed.
         */
        void graphDestroyed(ServerWorld world, GraphWorld controller, long id);
    }
}
