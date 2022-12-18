package com.kneelawk.graphlib.client;

import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.render.DebugRenderer;
import com.mojang.blaze3d.platform.InputUtil;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBind;

import java.util.Set;

@SuppressWarnings("unused")
public class GraphLibFabricModClient implements ClientModInitializer {
    private static final LongSet loadedChunks = new LongLinkedOpenHashSet();

    public static final KeyBind DEBUG_NODE_INTERACT =
        new KeyBind("key.graphlib.debug_node_inspect", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getKeyCode(),
            "category.graphlib.graphlib");

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        GraphLibClient.register();

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

                        GraphLibClient.removeGraphChunks(graph);
                    }
                }
            }
        });

        KeyBindingHelper.registerKeyBinding(DEBUG_NODE_INTERACT);
    }
}
