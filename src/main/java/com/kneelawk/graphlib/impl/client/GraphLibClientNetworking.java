package com.kneelawk.graphlib.impl.client;

import com.kneelawk.graphlib.api.v1.client.GraphLibClient;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.api.v1.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.v1.graph.struct.Graph;
import com.kneelawk.graphlib.api.v1.graph.struct.Node;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketDecoder;
import com.kneelawk.graphlib.impl.GraphLibCommonNetworking;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GraphLibClientNetworking {
    private GraphLibClientNetworking() {
    }

    private static final Map<Integer, Identifier> idMap = Collections.synchronizedMap(new Int2ObjectLinkedOpenHashMap<>());

    public static final BlockNodePacketDecoder DEFAULT_DECODER = buf -> {
        int hashCode = buf.readInt();
        int classHash = buf.readInt();

        byte type = buf.readByte();

        return switch (type) {
            case 0 -> new SimpleClientBlockNode(hashCode, classHash);
            case 1 -> new SimpleClientSidedBlockNode(hashCode, classHash, Direction.byId(buf.readByte()));
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

                ClientBlockGraph debugGraph = decodeBlockGraph(buf);
                if (debugGraph == null) return;

                client.execute(() -> addBlockGraph(debugGraph));
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.GRAPH_UPDATE_BULK_ID,
            (client, handler, buf, responseSender) -> {

                int graphCount = buf.readVarInt();
                List<ClientBlockGraph> graphs = new ArrayList<>(graphCount);

                for (int i = 0; i < graphCount; i++) {
                    ClientBlockGraph debugGraph = decodeBlockGraph(buf);
                    if (debugGraph == null) return;
                    graphs.add(debugGraph);
                }

                client.execute(() -> {
                    for (ClientBlockGraph debugGraph : graphs) {
                        addBlockGraph(debugGraph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.GRAPH_DESTROY_ID,
            (client, handler, buf, responseSender) -> {
                long graphId = buf.readLong();

                client.execute(() -> {
                    ClientBlockGraph graph = GraphLibClient.DEBUG_GRAPHS.remove(graphId);

                    if (graph != null) {
                        GraphLibClientImpl.removeGraphChunks(graph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GraphLibCommonNetworking.DEBUGGING_STOP_ID,
            (client, handler, buf, responseSender) -> client.execute(() -> {
                GraphLibClient.DEBUG_GRAPHS.clear();
                GraphLibClient.GRAPHS_PER_CHUNK.clear();
            }));
    }

    @Nullable
    private static ClientBlockGraph decodeBlockGraph(PacketByteBuf buf) {
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

            BlockNodePacketDecoder decoder = GraphLibClient.BLOCK_NODE_PACKET_DECODER.get(nodeTypeId);
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

        return new ClientBlockGraph(graphId, graph, chunks);
    }

    private static void addBlockGraph(ClientBlockGraph debugGraph) {
        ClientBlockGraph oldGraph = GraphLibClient.DEBUG_GRAPHS.put(debugGraph.graphId(), debugGraph);

        if (oldGraph != null) {
            GraphLibClientImpl.removeGraphChunks(oldGraph);
        }

        for (long chunk : debugGraph.chunks()) {
            Set<ClientBlockGraph> chunkSet = GraphLibClient.GRAPHS_PER_CHUNK.get(chunk);
            if (chunkSet == null) {
                chunkSet = new LinkedHashSet<>();
                chunkSet.add(debugGraph);
                GraphLibClient.GRAPHS_PER_CHUNK.put(chunk, chunkSet);
            } else {
                chunkSet.add(debugGraph);
            }
        }
    }
}
