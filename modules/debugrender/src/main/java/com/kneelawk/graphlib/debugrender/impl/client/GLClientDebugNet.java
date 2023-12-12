/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
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

package com.kneelawk.graphlib.debugrender.impl.client;

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

import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.graph.Graph;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.debugrender.api.client.BlockNodeDebugPacketDecoder;
import com.kneelawk.graphlib.debugrender.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.debug.graph.SimpleDebugBlockGraph;
import com.kneelawk.graphlib.debugrender.impl.client.debug.graph.SimpleDebugBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.debug.graph.SimpleDebugSidedBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.debug.render.DebugRenderer;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.debugrender.impl.GLDebugNet;

public final class GLClientDebugNet {
    private GLClientDebugNet() {
    }

    private static final Map<Integer, Identifier> idMap =
        Collections.synchronizedMap(new Int2ObjectLinkedOpenHashMap<>());

    public static final BlockNodeDebugPacketDecoder DEFAULT_DECODER = buf -> {
        int hashCode = buf.readInt();
        int color = buf.readInt();

        byte type = buf.readByte();

        return switch (type) {
            case 0 -> new SimpleDebugBlockNode(hashCode, color);
            case 1 -> new SimpleDebugSidedBlockNode(hashCode, color, Direction.byId(buf.readByte()));
            default -> {
                GLLog.error("Attempted default BlockNode decoding but encountered unknown default id: {}", type);
                yield null;
            }
        };
    };

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.ID_MAP_BULK_ID,
            (client, handler, buf, responseSender) -> {
                int size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    Identifier id = buf.readIdentifier();
                    int index = buf.readVarInt();
                    idMap.put(index, id);
                }
            });
        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.ID_MAP_PUT_ID,
            (client, handler, buf, responseSender) -> {
                Identifier id = buf.readIdentifier();
                int index = buf.readVarInt();
                idMap.put(index, id);
            });

        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.GRAPH_UPDATE_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                DebugBlockGraph debugGraph = decodeBlockGraph(universeId, buf);
                if (debugGraph == null) return;

                client.execute(() -> addBlockGraph(universeId, debugGraph));
            });
        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.GRAPH_UPDATE_BULK_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                int graphCount = buf.readVarInt();
                List<DebugBlockGraph> graphs = new ArrayList<>(graphCount);

                for (int i = 0; i < graphCount; i++) {
                    DebugBlockGraph debugGraph = decodeBlockGraph(universeId, buf);
                    if (debugGraph == null) return;
                    graphs.add(debugGraph);
                }

                client.execute(() -> {
                    for (DebugBlockGraph debugGraph : graphs) {
                        addBlockGraph(universeId, debugGraph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.GRAPH_DESTROY_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();
                Identifier universeId = idMap.get(universeInt);
                if (universeId == null) {
                    GLLog.error("Received unknown universe id: {}", universeInt);
                    return;
                }

                long graphId = buf.readLong();

                client.execute(() -> {
                    Long2ObjectMap<DebugBlockGraph> universe = DebugRenderer.DEBUG_GRAPHS.get(universeId);
                    if (universe == null) {
                        GLLog.warn("Received GRAPH_DESTROY for un-tracked universe: {}", universeId);
                        return;
                    }

                    DebugBlockGraph graph = universe.remove(graphId);

                    if (universe.isEmpty()) {
                        DebugRenderer.DEBUG_GRAPHS.remove(universeId);
                    }

                    if (graph != null) {
                        GraphLibDebugRenderClientImpl.removeGraphChunks(graph);
                    }
                });
            });
        ClientPlayNetworking.registerGlobalReceiver(GLDebugNet.DEBUGGING_STOP_ID,
            (client, handler, buf, responseSender) -> {
                int universeInt = buf.readVarInt();

                client.execute(() -> {
                    Identifier universeId = idMap.get(universeInt);
                    if (universeId == null) {
                        GLLog.error("Received unknown universe id: {}", universeInt);
                        return;
                    }

                    Long2ObjectMap<DebugBlockGraph> universe = DebugRenderer.DEBUG_GRAPHS.remove(universeId);

                    if (DebugRenderer.DEBUG_GRAPHS.isEmpty()) {
                        GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.clear();
                    } else {
                        if (universe == null) {
                            GLLog.warn("Received DEBUGGING_STOP for un-tracked universe: {}", universeId);
                            return;
                        }

                        for (DebugBlockGraph graph : universe.values()) {
                            GraphLibDebugRenderClientImpl.removeGraphChunks(graph);
                        }
                    }
                });
            });
    }

    @Nullable
    private static DebugBlockGraph decodeBlockGraph(Identifier universeId, PacketByteBuf buf) {
        Graph<ClientBlockNodeHolder, EmptyLinkKey> graph = new Graph<>();
        List<Node<ClientBlockNodeHolder, EmptyLinkKey>> nodeList = new ArrayList<>();
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

            BlockNodeDebugPacketDecoder decoder = GraphLibDebugRenderClientImpl.getDebugDecoder(universeId, nodeTypeId);
            if (decoder == null) {
                decoder = DEFAULT_DECODER;
            }

            DebugBlockNode data = decoder.fromPacket(buf);
            if (data == null) {
                GLLog.error("Unable to decode BlockNode packet for {}", nodeTypeId);
                return null;
            }

            Node<ClientBlockNodeHolder, EmptyLinkKey> node = graph.add(new ClientBlockNodeHolder(pos, data, graphId));
            nodeList.add(node);

            chunks.add(ChunkPos.toLong(pos));
        }

        int linkCount = buf.readVarInt();
        for (int i = 0; i < linkCount; i++) {
            int nodeAIndex = buf.readVarInt();
            int nodeBIndex = buf.readVarInt();

            if (nodeAIndex < 0 || nodeAIndex >= nodeList.size()) {
                GLLog.warn("Received packet with invalid links. Node {} points to nothing.", nodeAIndex);
                continue;
            }

            if (nodeBIndex < 0 || nodeBIndex >= nodeList.size()) {
                GLLog.warn("Received packet with invalid links. Node {} points to nothing.", nodeBIndex);
                continue;
            }

            Node<ClientBlockNodeHolder, EmptyLinkKey> nodeA = nodeList.get(nodeAIndex);
            Node<ClientBlockNodeHolder, EmptyLinkKey> nodeB = nodeList.get(nodeBIndex);

            graph.link(nodeA, nodeB, EmptyLinkKey.INSTANCE);
        }

        return new SimpleDebugBlockGraph(universeId, graphId, graph, chunks);
    }

    private static void addBlockGraph(Identifier universeId, DebugBlockGraph debugGraph) {
        Long2ObjectMap<DebugBlockGraph> universe = DebugRenderer.DEBUG_GRAPHS.get(universeId);
        if (universe == null) {
            universe = new Long2ObjectLinkedOpenHashMap<>();
            DebugRenderer.DEBUG_GRAPHS.put(universeId, universe);
        }

        DebugBlockGraph oldGraph = universe.put(debugGraph.graphId(), debugGraph);

        if (oldGraph != null) {
            GraphLibDebugRenderClientImpl.removeGraphChunks(oldGraph);
        }

        for (long chunk : debugGraph.chunks()) {
            Set<DebugBlockGraph> chunkSet = GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.get(chunk);
            if (chunkSet == null) {
                chunkSet = new LinkedHashSet<>();
                chunkSet.add(debugGraph);
                GraphLibDebugRenderClientImpl.GRAPHS_PER_CHUNK.put(chunk, chunkSet);
            } else {
                chunkSet.add(debugGraph);
            }
        }
    }
}
