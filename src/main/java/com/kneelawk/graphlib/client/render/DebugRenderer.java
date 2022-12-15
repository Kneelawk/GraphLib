package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.GraphLibClient;
import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Link;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.mixin.api.RenderLayerHelper;
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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;

public final class DebugRenderer {
    private DebugRenderer() {
    }

    private static @Nullable Framebuffer framebuffer = null;
    private static final Object2ObjectMap<RenderLayer, BufferBuilder> layerMap = new Object2ObjectLinkedOpenHashMap<>();
    private static final VertexConsumerProvider.Immediate immediate =
        VertexConsumerProvider.immediate(layerMap, new BufferBuilder(256));

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
        WorldRenderEvents.LAST.register(DebugRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        if (GraphLibClient.DEBUG_GRAPHS.isEmpty()) {
            return;
        }

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

        for (ClientBlockGraph graph : GraphLibClient.DEBUG_GRAPHS.values()) {
            int graphColor = graphColor(graph.graphId());
            Object2ObjectMap<Node<ClientBlockNodeHolder>, Vec3d> endpoints =
                new Object2ObjectLinkedOpenHashMap<>(graph.graph().size());
            ObjectSet<Link<ClientBlockNodeHolder>> links = new ObjectLinkedOpenHashSet<>();

            for (var node : graph.graph()) {
                ClientBlockNode cbn = node.data().node();
                BlockNodeRendererHolder<?> renderer = GraphLibClient.BLOCK_NODE_RENDERER.get(cbn.getRenderId());
                if (renderer == null) continue;

                // FIXME passes dud arguments
                Vec3d endpoint = renderer.getLineEndpoint(cbn, node, graph, 0, 0, List.of());
                endpoints.put(node, endpoint);

                BlockPos origin = node.data().pos();

                stack.push();
                stack.translate(origin.getX(), origin.getY(), origin.getZ());

                renderer.render(cbn, node, immediate, stack, graph, endpoint, graphColor);

                stack.pop();

                links.addAll(node.connections());
            }

            VertexConsumer consumer = immediate.getBuffer(Layers.DEBUG_LINES);

            for (var link : links) {
                var nodeA = link.first();
                var nodeB = link.second();

                if (!endpoints.containsKey(nodeA) || !endpoints.containsKey(nodeB)) continue;

                Vec3d endpointA = endpoints.get(nodeA);
                Vec3d endpointB = endpoints.get(nodeB);
                BlockPos posA = nodeA.data().pos();
                BlockPos posB = nodeB.data().pos();

                drawLine(stack, consumer, (float) (posA.getX() + endpointA.x), (float) (posA.getY() + endpointA.y),
                    (float) (posA.getZ() + endpointA.z), (float) (posB.getX() + endpointB.x),
                    (float) (posB.getY() + endpointB.y), (float) (posB.getZ() + endpointB.z), graphColor);
            }
        }

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

    public static void drawCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float width, float height, float depth, int color) {
        drawCube(stack, consumer, x, y, z, width, 0f, 0f, 0f, height, 0f, 0f, 0f, depth, color);
    }

    public static void drawCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                float radX2, float radY2, float radZ2, int color) {
        drawRect(stack, consumer, x - radX2, y - radY2, z - radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        drawRect(stack, consumer, x + radX2, y + radY2, z + radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        drawLine(stack, consumer, x - radX0 - radX1 - radX2, y - radY0 - radY1 - radY2, z - radZ0 - radZ1 - radZ2,
            x - radX0 - radX1 + radX2, y - radY0 - radY1 + radY2, z - radZ0 - radZ1 + radZ2, color);
        drawLine(stack, consumer, x - radX0 + radX1 - radX2, y - radY0 + radY1 - radY2, z - radZ0 + radZ1 - radZ2,
            x - radX0 + radX1 + radX2, y - radY0 + radY1 + radY2, z - radZ0 + radZ1 + radZ2, color);
        drawLine(stack, consumer, x + radX0 + radX1 - radX2, y + radY0 + radY1 - radY2, z + radZ0 + radZ1 - radZ2,
            x + radX0 + radX1 + radX2, y + radY0 + radY1 + radY2, z + radZ0 + radZ1 + radZ2, color);
        drawLine(stack, consumer, x + radX0 - radX1 - radX2, y + radY0 - radY1 - radY2, z + radZ0 - radZ1 - radZ2,
            x + radX0 - radX1 + radX2, y + radY0 - radY1 + radY2, z + radZ0 - radZ1 + radZ2, color);
    }

    public static void drawRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                int color) {
        drawLine(stack, consumer, x - radX0 - radX1, y - radY0 - radY1, z - radZ0 - radZ1, x - radX0 + radX1,
            y - radY0 + radY1, z - radZ0 + radZ1, color);
        drawLine(stack, consumer, x - radX0 + radX1, y - radY0 + radY1, z - radZ0 + radZ1, x + radX0 + radX1,
            y + radY0 + radY1, z + radZ0 + radZ1, color);
        drawLine(stack, consumer, x + radX0 + radX1, y + radY0 + radY1, z + radZ0 + radZ1, x + radX0 - radX1,
            y + radY0 - radY1, z + radZ0 - radZ1, color);
        drawLine(stack, consumer, x + radX0 - radX1, y + radY0 - radY1, z + radZ0 - radZ1, x - radX0 - radX1,
            y - radY0 - radY1, z - radZ0 - radZ1, color);
    }

    public static void drawLine(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x0, float y0,
                                float z0, float x1, float y1, float z1, int color) {
        Matrix4f model = stack.peek().getPosition();
        Matrix3f normal = stack.peek().getNormal();

        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float fact = MathHelper.fastInverseSqrt(dx * dx + dy * dy + dz * dz);
        dx *= fact;
        dy *= fact;
        dz *= fact;

        consumer.vertex(model, x0, y0, z0).color(color).normal(normal, dx, dy, dz).next();
        consumer.vertex(model, x1, y1, z1).color(color).normal(normal, dx, dy, dz).next();
    }

    public static void fillCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float width, float height, float depth, int color) {
        fillCube(stack, consumer, x, y, z, width, 0f, 0f, 0f, height, 0f, 0f, 0f, depth, color);
    }

    public static void fillCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                float radX2, float radY2, float radZ2, int color) {
        fillRect(stack, consumer, x - radX1, y - radY1, z - radZ1, radX0, radY0, radZ0, radX2, radY2, radZ2, color);
        fillRect(stack, consumer, x + radX1, y + radY1, z + radZ1, radX0, radY0, radZ0, -radX2, -radY2, -radZ2, color);
        fillRect(stack, consumer, x - radX2, y - radY2, z - radZ2, -radX0, -radY0, -radZ0, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x + radX2, y + radY2, z + radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x - radX0, y - radY0, z - radZ0, radX2, radY2, radZ2, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x + radX0, y + radY0, z + radZ0, -radX2, -radY2, -radZ2, radX1, radY1, radZ1, color);
    }

    public static void fillRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                int color) {
        Matrix4f model = stack.peek().getPosition();

        consumer.vertex(model, x - radX0 + radX1, y - radY0 + radY1, z - radZ0 + radZ1).color(color).next();
        consumer.vertex(model, x - radX0 - radX1, y - radY0 - radY1, z - radZ0 - radZ1).color(color).next();
        consumer.vertex(model, x + radX0 - radX1, y + radY0 - radY1, z + radZ0 - radZ1).color(color).next();
        consumer.vertex(model, x + radX0 + radX1, y + radY0 + radY1, z + radZ0 + radZ1).color(color).next();
    }

    public static int graphColor(long graphId) {
        Random rand = new Random(graphId);
        return rand.nextInt() | 0xFF000000;
    }
}
