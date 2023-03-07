package com.kneelawk.graphlib;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.graph.BlockGraph;
import com.kneelawk.graphlib.graph.BlockGraphController;

public final class GraphLibEvents {
    private GraphLibEvents() {
    }

    public static final Event<GraphCreatedListener> GRAPH_CREATED =
        EventFactory.createArrayBacked(GraphCreatedListener.class, (world, controller, graph) -> {
        }, listeners -> (world, controller, graph) -> {
            for (GraphCreatedListener listener : listeners) {
                listener.graphCreated(world, controller, graph);
            }
        });

    public static final Event<GraphUpdatedListener> GRAPH_UPDATED =
        EventFactory.createArrayBacked(GraphUpdatedListener.class, (world, controller, graph) -> {
        }, listeners -> (world, controller, graph) -> {
            for (GraphUpdatedListener listener : listeners) {
                listener.graphUpdated(world, controller, graph);
            }
        });

    public static final Event<GraphDestroyedListener> GRAPH_DESTROYED =
        EventFactory.createArrayBacked(GraphDestroyedListener.class, (world, controller, id) -> {
        }, listeners -> (world, controller, id) -> {
            for (GraphDestroyedListener listener : listeners) {
                listener.graphDestroyed(world, controller, id);
            }
        });

    public interface GraphCreatedListener {
        void graphCreated(ServerWorld world, BlockGraphController controller, BlockGraph graph);
    }

    public interface GraphUpdatedListener {
        void graphUpdated(ServerWorld world, BlockGraphController controller, BlockGraph graph);
    }

    public interface GraphDestroyedListener {
        void graphDestroyed(ServerWorld world, BlockGraphController controller, long id);
    }
}
