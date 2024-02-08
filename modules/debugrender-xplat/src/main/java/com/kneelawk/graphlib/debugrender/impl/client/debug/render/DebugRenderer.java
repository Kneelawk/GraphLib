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

package com.kneelawk.graphlib.debugrender.impl.client.debug.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Link;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.debugrender.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.client.render.RenderUtils;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;
import com.kneelawk.graphlib.debugrender.api.graph.SidedDebugBlockNode;
import com.kneelawk.graphlib.debugrender.impl.client.GraphLibDebugRenderClientImpl;
import com.kneelawk.graphlib.debugrender.impl.mixin.api.RenderLayerHelper;

public final class DebugRenderer {
    /**
     * Map of graph id long to graph for all currently debugging graphs.
     */
    public static final Map<Identifier, Long2ObjectMap<DebugBlockGraph>> DEBUG_GRAPHS = new LinkedHashMap<>();

    private DebugRenderer() {
    }

    private sealed interface NPos {}

    private record NBlockPos(BlockPos pos) implements NPos {}

    private record NSidedPos(SidedPos pos) implements NPos {}

    private static class NPosData {
        int nodeCount = 0;
        List<Vec3d> endpoints = new ArrayList<>();
    }

    public static final class Layers extends RenderPhase {
        private Layers(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }

        public static final RenderLayer DEBUG_LINES = RenderLayerHelper.of(
            "debug_lines",
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            256,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                .shader(RenderPhase.LINES_SHADER)
                .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .writeMaskState(RenderPhase.ALL_MASK)
                .cull(RenderPhase.DISABLE_CULLING)
                .build(false)
        );

        public static final RenderLayer DEBUG_QUADS = RenderLayerHelper.of(
            "debug_quads",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                .shader(RenderPhase.COLOR_SHADER)
                .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                .writeMaskState(RenderPhase.ALL_MASK)
                .cull(RenderPhase.DISABLE_CULLING)
                .build(false)
        );
    }

    public static void render(MatrixStack stack, Vec3d camPos, VertexConsumerProvider consumers) {
        if (DEBUG_GRAPHS.isEmpty()) {
            return;
        }

        stack.push();
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        renderGraphs(stack, consumers);

        stack.pop();
    }

    private static void renderGraphs(MatrixStack stack, VertexConsumerProvider consumers) {
        Map<NPos, NPosData> nodeEndpoints = new HashMap<>();

        for (Long2ObjectMap<DebugBlockGraph> universe : DEBUG_GRAPHS.values()) {
            for (DebugBlockGraph graph : universe.values()) {
                for (var node : graph.graph()) {
                    DebugBlockNode cbn = node.data().node();

                    NPos pos;
                    if (cbn instanceof SidedDebugBlockNode scbn) {
                        pos = new NSidedPos(new SidedPos(node.data().pos(), scbn.getSide()));
                    } else {
                        pos = new NBlockPos(node.data().pos());
                    }

                    nodeEndpoints.computeIfAbsent(pos, nPos -> new NPosData()).nodeCount++;
                }
            }
        }

        for (Long2ObjectMap<DebugBlockGraph> universe : DEBUG_GRAPHS.values()) {
            for (DebugBlockGraph graph : universe.values()) {
                int graphColor = RenderUtils.graphColor(graph.graphId());
                Object2ObjectMap<Node<ClientBlockNodeHolder, EmptyLinkKey>, Vec3d> endpoints =
                    new Object2ObjectLinkedOpenHashMap<>(graph.graph().size());
                ObjectSet<Link<ClientBlockNodeHolder, EmptyLinkKey>> links = new ObjectLinkedOpenHashSet<>();

                for (var node : graph.graph()) {
                    DebugBlockNode cbn = node.data().node();
                    BlockNodeDebugRendererHolder<?> renderer =
                        GraphLibDebugRenderClientImpl.getDebugRenderer(graph.universeId(), cbn.getRenderId());
                    if (renderer == null) continue;

                    NPos pos;
                    if (cbn instanceof SidedDebugBlockNode scbn) {
                        pos = new NSidedPos(new SidedPos(node.data().pos(), scbn.getSide()));
                    } else {
                        pos = new NBlockPos(node.data().pos());
                    }

                    // should never be null unless GraphLibClient.DEBUG_GRAPHS was modified by another thread
                    NPosData data = nodeEndpoints.get(pos);

                    Vec3d endpoint =
                        renderer.getLineEndpoint(cbn, node, graph, data.nodeCount, data.endpoints.size(),
                            data.endpoints);
                    endpoints.put(node, endpoint);
                    data.endpoints.add(endpoint);

                    BlockPos origin = node.data().pos();

                    stack.push();
                    stack.translate(origin.getX(), origin.getY(), origin.getZ());

                    renderer.render(cbn, node, consumers, stack, graph, endpoint, graphColor);

                    stack.pop();

                    links.addAll(node.connections());
                }

                VertexConsumer consumer = consumers.getBuffer(Layers.DEBUG_LINES);

                for (var link : links) {
                    var nodeA = link.first();
                    var nodeB = link.second();

                    if (!endpoints.containsKey(nodeA) || !endpoints.containsKey(nodeB)) continue;

                    Vec3d endpointA = endpoints.get(nodeA);
                    Vec3d endpointB = endpoints.get(nodeB);
                    BlockPos posA = nodeA.data().pos();
                    BlockPos posB = nodeB.data().pos();

                    RenderUtils.drawLine(stack, consumer, (float) (posA.getX() + endpointA.x),
                        (float) (posA.getY() + endpointA.y),
                        (float) (posA.getZ() + endpointA.z), (float) (posB.getX() + endpointB.x),
                        (float) (posB.getY() + endpointB.y), (float) (posB.getZ() + endpointB.z), graphColor);
                }
            }
        }
    }
}
