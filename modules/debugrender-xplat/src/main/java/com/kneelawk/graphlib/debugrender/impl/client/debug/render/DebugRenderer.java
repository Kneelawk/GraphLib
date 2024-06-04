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

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

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
import com.kneelawk.kmodlib.client.overlay.RenderToOverlay;

public final class DebugRenderer {
    /**
     * Map of graph id long to graph for all currently debugging graphs.
     */
    public static final Map<ResourceLocation, Long2ObjectMap<DebugBlockGraph>> DEBUG_GRAPHS = new LinkedHashMap<>();

    private DebugRenderer() {
    }

    private sealed interface NPos {}

    private record NBlockPos(BlockPos pos) implements NPos {}

    private record NSidedPos(SidedPos pos) implements NPos {}

    private static class NPosData {
        int nodeCount = 0;
        List<Vec3> endpoints = new ArrayList<>();
    }

    static final class Layers extends RenderStateShard {
        private Layers(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }

        public static final RenderType DEBUG_LINES =
            RenderLayerHelper.of("debug_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256,
                false, false,
                RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL).createCompositeState(false));

        public static final RenderType DEBUG_QUADS =
            RenderLayerHelper.of("debug_quads", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false,
                false, RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL).createCompositeState(false));
    }

    public static void init() {
        RenderToOverlay.LAYER_MAP.put(Layers.DEBUG_LINES,
            new ByteBufferBuilder(Layers.DEBUG_LINES.bufferSize()));
        RenderToOverlay.LAYER_MAP.put(Layers.DEBUG_QUADS,
            new ByteBufferBuilder(Layers.DEBUG_QUADS.bufferSize()));
        RenderToOverlay.EVENT.register(
            ctx -> render(ctx.stack(), ctx.modelViewMatrix(), ctx.camera().getPosition(), ctx.buffers()));
    }

    public static void render(PoseStack stack, Matrix4f modelViewMatrix, Vec3 camPos, MultiBufferSource consumers) {
        if (DEBUG_GRAPHS.isEmpty()) {
            return;
        }

        stack.pushPose();
        stack.mulPose(modelViewMatrix);
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        renderGraphs(stack, consumers);

        stack.popPose();
    }

    private static void renderGraphs(PoseStack stack, MultiBufferSource consumers) {
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
                Object2ObjectMap<Node<ClientBlockNodeHolder, EmptyLinkKey>, Vec3> endpoints =
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

                    Vec3 endpoint = renderer.getLineEndpoint(cbn, node, graph, data.nodeCount, data.endpoints.size(),
                        data.endpoints);
                    endpoints.put(node, endpoint);
                    data.endpoints.add(endpoint);

                    BlockPos origin = node.data().pos();

                    stack.pushPose();
                    stack.translate(origin.getX(), origin.getY(), origin.getZ());

                    renderer.render(cbn, node, consumers, stack, graph, endpoint, graphColor);

                    stack.popPose();

                    links.addAll(node.connections());
                }

                VertexConsumer consumer = consumers.getBuffer(Layers.DEBUG_LINES);

                for (var link : links) {
                    var nodeA = link.first();
                    var nodeB = link.second();

                    if (!endpoints.containsKey(nodeA) || !endpoints.containsKey(nodeB)) continue;

                    Vec3 endpointA = endpoints.get(nodeA);
                    Vec3 endpointB = endpoints.get(nodeB);
                    BlockPos posA = nodeA.data().pos();
                    BlockPos posB = nodeB.data().pos();

                    RenderUtils.drawLine(stack, consumer, (float) (posA.getX() + endpointA.x),
                        (float) (posA.getY() + endpointA.y), (float) (posA.getZ() + endpointA.z),
                        (float) (posB.getX() + endpointB.x), (float) (posB.getY() + endpointB.y),
                        (float) (posB.getZ() + endpointB.z), graphColor);
                }
            }
        }
    }
}
