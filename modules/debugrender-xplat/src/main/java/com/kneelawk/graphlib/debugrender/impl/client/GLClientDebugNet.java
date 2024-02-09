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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.util.Identifier;
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
import com.kneelawk.graphlib.debugrender.impl.payload.DebuggingStopPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphDestroyPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdateBulkPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdatePayload;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadGraph;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadHeader;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadLink;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadNode;
import com.kneelawk.graphlib.impl.GLLog;

public final class GLClientDebugNet {
    private GLClientDebugNet() {
    }

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

    public static void onGraphUpdate(GraphUpdatePayload payload, Executor clientEx) {
        DebugBlockGraph debugGraph = decodeBlockGraph(payload.header(), payload.graph());
        if (debugGraph == null) return;

        clientEx.execute(() -> addBlockGraph(payload.header().universeId(), debugGraph));
    }

    public static void onGraphUpdateBulk(GraphUpdateBulkPayload payload, Executor clientEx) {
        List<DebugBlockGraph> graphs = new ObjectArrayList<>(payload.graphs().size());
        for (PayloadGraph graph : payload.graphs()) {
            DebugBlockGraph debugGraph = decodeBlockGraph(payload.header(), graph);
            if (debugGraph == null) return;
            graphs.add(debugGraph);
        }

        clientEx.execute(() -> {
            for (DebugBlockGraph debugGraph : graphs) {
                addBlockGraph(payload.header().universeId(), debugGraph);
            }
        });
    }

    public static void onGraphDestroy(GraphDestroyPayload payload, Executor clientEx) {
        clientEx.execute(() -> {
            Identifier universeId = payload.universeId();
            long graphId = payload.graphId();

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
    }

    public static void onDebugginStop(DebuggingStopPayload payload, Executor clientEx) {
        clientEx.execute(() -> {
            Identifier universeId = payload.universeId();

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
    }

    private static @Nullable DebugBlockGraph decodeBlockGraph(PayloadHeader header, PayloadGraph payload) {
        Graph<ClientBlockNodeHolder, EmptyLinkKey> graph = new Graph<>();
        List<Node<ClientBlockNodeHolder, EmptyLinkKey>> nodeList = new ArrayList<>();
        LongSet chunks = new LongLinkedOpenHashSet();

        for (PayloadNode node : payload.nodes()) {
            int nodeTypeInt = node.typeId();
            Identifier nodeTypeId = header.palette().get(nodeTypeInt);
            if (nodeTypeId == null) {
                GLLog.error("Received unknown BlockNode id: {}", nodeTypeInt);
                return null;
            }

            BlockNodeDebugPacketDecoder decoder =
                GraphLibDebugRenderClientImpl.getDebugDecoder(header.universeId(), nodeTypeId);
            if (decoder == null) {
                decoder = DEFAULT_DECODER;
            }

            DebugBlockNode data = decoder.fromPacket(header.nodeData());
            if (data == null) {
                GLLog.error("Unable to decode BlockNode packet for {}", nodeTypeId);
                return null;
            }

            Node<ClientBlockNodeHolder, EmptyLinkKey> debugNode =
                graph.add(new ClientBlockNodeHolder(node.pos(), data, payload.graphId()));
            nodeList.add(debugNode);

            chunks.add(ChunkPos.toLong(node.pos()));
        }

        for (PayloadLink link : payload.links()) {
            int nodeAIndex = link.nodeA();
            int nodeBIndex = link.nodeB();

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

        return new SimpleDebugBlockGraph(header.universeId(), payload.graphId(), graph, chunks);
    }

    private static void addBlockGraph(Identifier universeId, DebugBlockGraph debugGraph) {
        Long2ObjectMap<DebugBlockGraph> universe =
            DebugRenderer.DEBUG_GRAPHS.computeIfAbsent(universeId, k -> new Long2ObjectLinkedOpenHashMap<>());

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
