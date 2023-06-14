package com.kneelawk.graphlib.impl.client;

import java.util.Set;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import com.kneelawk.graphlib.impl.client.debug.graph.DebugBlockGraph;
import com.kneelawk.graphlib.impl.client.debug.render.DebugRenderer;

@SuppressWarnings("unused")
public class GraphLibFabricModClient implements ClientModInitializer {
    private final LongSet loadedChunks = new LongLinkedOpenHashSet();

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        GraphLibClientImpl.register();

        ClientProxy.init();
        GLClientDebugNet.init();

        DebugRenderer.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibClientImpl.GRAPHS_PER_CHUNK.clear();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DebugRenderer.DEBUG_GRAPHS.clear();
            GraphLibClientImpl.GRAPHS_PER_CHUNK.clear();
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> loadedChunks.add(chunk.getPos().toLong()));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            long chunkLong = chunk.getPos().toLong();
            loadedChunks.remove(chunkLong);

            Set<DebugBlockGraph> graphs = GraphLibClientImpl.GRAPHS_PER_CHUNK.get(chunkLong);
            if (graphs != null) {
                for (DebugBlockGraph graph : graphs) {
                    boolean anyChunkLoaded = false;
                    for (long graphChunk : graph.chunks()) {
                        if (loadedChunks.contains(graphChunk)) {
                            anyChunkLoaded = true;
                            break;
                        }
                    }

                    if (!anyChunkLoaded) {
                        Long2ObjectMap<DebugBlockGraph> universe =
                            DebugRenderer.DEBUG_GRAPHS.get(graph.universeId());
                        universe.remove(graph.graphId());
                        if (universe.isEmpty()) {
                            DebugRenderer.DEBUG_GRAPHS.remove(graph.universeId());
                        }

                        GraphLibClientImpl.removeGraphChunks(graph);
                    }
                }
            }
        });
    }
}
