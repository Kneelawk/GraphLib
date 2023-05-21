package com.kneelawk.graphlib.impl.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.client.BlockNodePacketDecoder;
import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.util.graph.Graph;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibCommonNetworking;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientSidedBlockNode;

public final class GraphLibClientNetworking {
    private GraphLibClientNetworking() {
    }

    private static final Map<Integer, Identifier> idMap =
        Collections.synchronizedMap(new Int2ObjectLinkedOpenHashMap<>());

    public static final BlockNodePacketDecoder DEFAULT_DECODER = buf -> {
        int hashCode = buf.readInt();
        int color = buf.readInt();

        byte type = buf.readByte();

        return switch (type) {
            case 0 -> new SimpleClientBlockNode(hashCode, color);
            case 1 -> new SimpleClientSidedBlockNode(hashCode, color, Direction.byId(buf.readByte()));
            default -> {
                GLLog.error("Attempted default BlockNode decoding but encountered unknown default id: {}", type);
                yield null;
            }
        };
    };

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.ID_MAP_BULK_ID,
            (client, handler, buf, responseSender) -> {
                int size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    Identifier id = buf.readIdentifier();
                    int index = buf.readVarInt();
                    idMap.put(index, id);
                }
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.ID_MAP_PUT_ID,
            (client, handler, buf, responseSender) -> {
                Identifier id = buf.readIdentifier();
                int index = buf.readVarInt();
                idMap.put(index, id);
            });

        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.GRAPH_UPDATE_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                ClientBlockGraph debugGraph = decodeBlockGraph(universeId, buf);
                if (debugGraph == null) return;

                client.execute(() -> addBlockGraph(universeId, debugGraph));
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.GRAPH_UPDATE_BULK_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                int graphCount = buf.readVarInt();
                List<ClientBlockGraph> graphs = new ArrayList<>(graphCount);

                for (int i = 0; i < graphCount; i++) {
                    ClientBlockGraph debugGraph = decodeBlockGraph(universeId, buf);
                    if (debugGraph == null) return;
                    graphs.add(debugGraph);
                }

                client.execute(() -> {
                    for (ClientBlockGraph debugGraph : graphs) {
                        addBlockGraph(universeId, debugGraph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.GRAPH_DESTROY_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                long graphId = buf.readLong();

                client.execute(() -> {
                    Long2ObjectMap<ClientBlockGraph> universe = GraphLibClientImpl.DEBUG_GRAPHS.get(universeId);
                    if (universe == null) {
                        GLLog.warn("Received GRAPH_DESTROY for un-tracked universe: {}", universeId);
                        return;
                    }

                    ClientBlockGraph graph = universe.remove(graphId);

                    if (universe.isEmpty()) {
                        GraphLibClientImpl.DEBUG_GRAPHS.remove(universeId);
                    }

                    if (graph != null) {
                        GraphLibClientImpl.removeGraphChunks(graph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.DEBUGGING_STOP_ID,
            (client, handler, buf, responseSender) -> client.execute(() -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                Long2ObjectMap<ClientBlockGraph> universe = GraphLibClientImpl.DEBUG_GRAPHS.remove(universeId);

                if (GraphLibClientImpl.DEBUG_GRAPHS.isEmpty()) {
                    GraphLibClientImpl.GRAPHS_PER_CHUNK.clear();
                } else {
                    if (universe == null) {
                        GLLog.warn("Received DEBUGGING_STOP for un-tracked universe: {}", universeId);
                        return;
                    }

                    for (ClientBlockGraph graph : universe.values()) {
                        GraphLibClientImpl.removeGraphChunks(graph);
                    }
                }
            }));
    }

    @Nullable
    private static ClientBlockGraph decodeBlockGraph(Identifier universeId, PacketByteBuf buf) {
        Graph<ClientBlockNodeHolder> graph = new Graph<>();
        List<Node<ClientBlockNodeHolder>> nodeList = new ArrayList<>();
        LongSet chunks = new LongLinkedOpenHashSet();

        long graphId = buf.readLong();

        int nodeCount = buf.readVarInt();
        for (int i = 0; i < nodeCount; i++) {
            int nodeTypeInt = buf.readVarInt();
            Identifier nodeTypeId = idMap.get(nodeTypeInt);
            if (nodeTypeId == null) {
                GLLog.error("Received unknown BlockNode id: {}", nodeTypeInt);
                return null;
            }

            BlockPos pos = buf.readBlockPos();

            BlockNodePacketDecoder decoder = GraphLibClientImpl.getDecoder(universeId, nodeTypeId);
            if (decoder == null) {
                decoder = DEFAULT_DECODER;
            }

            ClientBlockNode data = decoder.fromPacket(buf);
            if (data == null) {
                GLLog.error("Unable to decode BlockNode packet for {}", nodeTypeId);
                return null;
            }

            Node<ClientBlockNodeHolder> node = graph.add(new ClientBlockNodeHolder(pos, data, graphId));
            nodeList.add(node);

            chunks.add(ChunkPos.toLong(pos));
        }

        int linkCount = buf.readVarInt();
        for (int i = 0; i < linkCount; i++) {
            Node<ClientBlockNodeHolder> nodeA = nodeList.get(buf.readVarInt());
            Node<ClientBlockNodeHolder> nodeB = nodeList.get(buf.readVarInt());

            graph.link(nodeA, nodeB);
        }

        return new ClientBlockGraph(universeId, graphId, graph, chunks);
    }

    private static void addBlockGraph(Identifier universeId, ClientBlockGraph debugGraph) {
        Long2ObjectMap<ClientBlockGraph> universe = GraphLibClientImpl.DEBUG_GRAPHS.get(universeId);
        if (universe == null) {
            universe = new Long2ObjectLinkedOpenHashMap<>();
            GraphLibClientImpl.DEBUG_GRAPHS.put(universeId, universe);
        }

        ClientBlockGraph oldGraph = universe.put(debugGraph.graphId(), debugGraph);

        if (oldGraph != null) {
            GraphLibClientImpl.removeGraphChunks(oldGraph);
        }

        for (long chunk : debugGraph.chunks()) {
            Set<ClientBlockGraph> chunkSet = GraphLibClientImpl.GRAPHS_PER_CHUNK.get(chunk);
            if (chunkSet == null) {
                chunkSet = new LinkedHashSet<>();
                chunkSet.add(debugGraph);
                GraphLibClientImpl.GRAPHS_PER_CHUNK.put(chunk, chunkSet);
            } else {
                chunkSet.add(debugGraph);
            }
        }
    }
}
