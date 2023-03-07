package com.kneelawk.graphlib.impl.client;

import java.util.Set;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import com.kneelawk.graphlib.api.v1.client.GraphLibClient;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.render.DebugRenderer;

@SuppressWarnings("unused")
public class GraphLibFabricModClient implements ClientModInitializer {
    private final LongSet loadedChunks = new LongLinkedOpenHashSet();

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        GraphLibClientImpl.register();

        GraphLibClientNetworking.init();

        DebugRenderer.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> GraphLibClient.DEBUG_GRAPHS.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> GraphLibClient.DEBUG_GRAPHS.clear());

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> loadedChunks.add(chunk.getPos().toLong()));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            long chunkLong = chunk.getPos().toLong();
            loadedChunks.remove(chunkLong);

            Set<ClientBlockGraph> graphs = GraphLibClient.GRAPHS_PER_CHUNK.get(chunkLong);
            if (graphs != null) {
                for (ClientBlockGraph graph : graphs) {
                    boolean anyChunkLoaded = false;
                    for (long graphChunk : graph.chunks()) {
                        if (loadedChunks.contains(graphChunk)) {
                            anyChunkLoaded = true;
                            break;
                        }
                    }

                    if (!anyChunkLoaded) {
                        GraphLibClient.DEBUG_GRAPHS.remove(graph.graphId());

                        GraphLibClientImpl.removeGraphChunks(graph);
                    }
                }
            }
        });
    }
}
