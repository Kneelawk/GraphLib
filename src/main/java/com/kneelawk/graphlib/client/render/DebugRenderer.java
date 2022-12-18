package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.GraphLibClient;
import com.kneelawk.graphlib.client.GraphLibFabricModClient;
import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.SidedClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Link;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.mixin.api.RenderLayerHelper;
import com.kneelawk.graphlib.util.PosWrapper;
import com.kneelawk.graphlib.util.SidedPos;
import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DebugRenderer {
    private DebugRenderer() {
    }

    private static @Nullable Framebuffer framebuffer = null;
    private static final Object2ObjectMap<RenderLayer, BufferBuilder> layerMap = new Object2ObjectLinkedOpenHashMap<>();
    private static final VertexConsumerProvider.Immediate immediate =
        VertexConsumerProvider.immediate(layerMap, new BufferBuilder(256));

    private static final Map<PosWrapper, NPosData> nodeData = new HashMap<>();
    private static final Map<ClientBlockNodeHolder, Vec3d> endpoints = new HashMap<>();
    private static boolean interactWasPressed = false;
    private static @Nullable ClientBlockNodeHolder hoveredNode = null;

    private static class NPosData {
        int nodeCount = 0;
        List<Vec3d> endpoints = new ArrayList<>();
    }

    static {
        layerMap.put(Layers.DEBUG_LINES, new BufferBuilder(Layers.DEBUG_LINES.getExpectedBufferSize()));
        layerMap.put(Layers.DEBUG_QUADS, new BufferBuilder(Layers.DEBUG_QUADS.getExpectedBufferSize()));
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

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(DebugRenderer::startTick);
        WorldRenderEvents.LAST.register(DebugRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(DebugRenderer::endTick);
    }

    private static void startTick(MinecraftClient client) {
        if (GraphLibClient.DEBUG_GRAPHS.isEmpty()) return;

        nodeData.clear();
        endpoints.clear();

        for (ClientBlockGraph graph : GraphLibClient.DEBUG_GRAPHS.values()) {
            for (var node : graph.graph()) {
                ClientBlockNode cbn = node.data().node();
                PosWrapper pos = getNPos(node, cbn);
                nodeData.computeIfAbsent(pos, nPos -> new NPosData()).nodeCount++;
            }
        }

        for (ClientBlockGraph graph : GraphLibClient.DEBUG_GRAPHS.values()) {
            for (var node : graph.graph()) {
                ClientBlockNodeHolder holder = node.data();
                ClientBlockNode cbn = holder.node();
                BlockNodeRendererHolder<?> renderer = GraphLibClient.BLOCK_NODE_RENDERER.get(cbn.getRenderId());
                if (renderer == null) continue;

                PosWrapper pos = getNPos(node, cbn);

                // should never be null unless GraphLibClient.DEBUG_GRAPHS was modified by another thread
                NPosData data = nodeData.get(pos);

                Vec3d endpoint =
                    renderer.getLineEndpoint(cbn, node, graph, data.nodeCount, data.endpoints.size(), data.endpoints);
                endpoints.put(holder, endpoint);
                data.endpoints.add(endpoint);
            }
        }
    }

    private static void endTick(MinecraftClient client) {
        if (GraphLibClient.DEBUG_GRAPHS.isEmpty()) return;

        if (GraphLibFabricModClient.DEBUG_NODE_INTERACT.isPressed()) {
            interactWasPressed = true;

            // do constant ray-casting
            hoveredNode = raycast();
        } else if (interactWasPressed) {
            interactWasPressed = false;

            // ray cast and select the cast node, opening the gui
            ClientBlockNodeHolder holder = raycast();
            hoveredNode = holder;
            if (holder != null) {
                // TODO: open gui
            }
        }
    }

    private static @Nullable ClientBlockNodeHolder raycast() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientPlayerEntity player = client.player;
        if (interactionManager == null) return null;
        if (player == null) return null;

        float tickDelta = client.getTickDelta();

        double reach = interactionManager.getReachDistance();
        double reachSqr = reach * reach;

        Vec3d rayStart = player.getCameraPosVec(tickDelta);
        Vec3d direction = player.getRotationVec(tickDelta);
        Vec3d rayEnd = rayStart.add(direction.multiply(reach));

        double closestSqr = reachSqr;
        ClientBlockNodeHolder closestNode = null;
        for (var graph : GraphLibClient.DEBUG_GRAPHS.values()) {
            for (var node : graph.graph()) {
                if (node.data().pos().getSquaredDistanceToCenter(player.getPos()) <= reachSqr) {
                    ClientBlockNodeHolder holder = node.data();
                    ClientBlockNode cbn = holder.node();
                    BlockNodeRendererHolder<?> renderer = GraphLibClient.BLOCK_NODE_RENDERER.get(cbn.getRenderId());
                    if (renderer == null) continue;

                    Vec3d endpoint = endpoints.get(holder);
                    if (endpoint == null) continue;

                    Box hitbox = renderer.getHitbox(cbn, node, graph, endpoint).offset(holder.pos());

                    if (hitbox.contains(rayStart) && closestSqr >= 0.0) {
                        return holder;
                    }

                    Optional<Vec3d> hit = hitbox.raycast(rayStart, rayEnd);
                    if (hit.isPresent()) {
                        double distanceSqr = rayStart.squaredDistanceTo(hit.get());
                        if (distanceSqr < closestSqr) {
                            closestSqr = distanceSqr;
                            closestNode = holder;
                        }
                    }
                }
            }
        }

        return closestNode;
    }

    private static void render(WorldRenderContext context) {
        if (GraphLibClient.DEBUG_GRAPHS.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (framebuffer == null) {
            framebuffer = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true,
                MinecraftClient.IS_SYSTEM_MAC);
            framebuffer.setClearColor(0f, 0f, 0f, 0f);
        }

        if (window.getFramebufferWidth() != framebuffer.textureWidth ||
            window.getFramebufferHeight() != framebuffer.textureHeight) {
            framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(),
                MinecraftClient.IS_SYSTEM_MAC);
        }

        framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

        MatrixStack mv = RenderSystem.getModelViewStack();
        mv.push();
        mv.loadIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();

        framebuffer.beginWrite(false);

        Vec3d camPos = context.camera().getPos();
        MatrixStack stack = context.matrixStack();
        stack.push();
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        renderGraphs(stack);

        stack.pop();

        immediate.draw();

        client.getFramebuffer().beginWrite(false);

        RenderSystem.enableDepthTest();
        mv.pop();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        framebuffer.draw(window.getFramebufferWidth(), window.getFramebufferHeight(), false);
        RenderSystem.disableBlend();
    }

    private static void renderGraphs(MatrixStack stack) {
        for (ClientBlockGraph graph : GraphLibClient.DEBUG_GRAPHS.values()) {
            int graphColor = RenderUtils.graphColor(graph.graphId());
            ObjectSet<Link<ClientBlockNodeHolder>> links = new ObjectLinkedOpenHashSet<>();

            for (var node : graph.graph()) {
                ClientBlockNodeHolder holder = node.data();
                ClientBlockNode cbn = holder.node();
                BlockNodeRendererHolder<?> renderer = GraphLibClient.BLOCK_NODE_RENDERER.get(cbn.getRenderId());
                if (renderer == null) continue;

                Vec3d endpoint = endpoints.get(holder);
                if (endpoint == null) continue;

                BlockPos origin = node.data().pos();

                stack.push();
                stack.translate(origin.getX(), origin.getY(), origin.getZ());

                renderer.render(cbn, node, immediate, stack, graph, endpoint, graphColor, hoveredNode == holder);

                stack.pop();

                links.addAll(node.connections());
            }

            VertexConsumer consumer = immediate.getBuffer(Layers.DEBUG_LINES);

            for (var link : links) {
                var nodeA = link.first().data();
                var nodeB = link.second().data();

                if (!endpoints.containsKey(nodeA) || !endpoints.containsKey(nodeB)) continue;

                Vec3d endpointA = endpoints.get(nodeA);
                Vec3d endpointB = endpoints.get(nodeB);
                BlockPos posA = nodeA.pos();
                BlockPos posB = nodeB.pos();

                RenderUtils.drawLine(stack, consumer, (float) (posA.getX() + endpointA.x),
                    (float) (posA.getY() + endpointA.y), (float) (posA.getZ() + endpointA.z),
                    (float) (posB.getX() + endpointB.x), (float) (posB.getY() + endpointB.y),
                    (float) (posB.getZ() + endpointB.z), graphColor);
            }
        }
    }

    @NotNull
    private static PosWrapper getNPos(Node<ClientBlockNodeHolder> node, ClientBlockNode cbn) {
        if (cbn instanceof SidedClientBlockNode scbn) {
            return new PosWrapper.Sided(new SidedPos(node.data().pos(), scbn.getSide()));
        } else {
            return new PosWrapper.Block(node.data().pos());
        }
    }
}
