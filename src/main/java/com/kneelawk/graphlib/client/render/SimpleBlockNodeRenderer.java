package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.client.graph.SimpleClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SimpleBlockNodeRenderer implements BlockNodeRenderer<SimpleClientBlockNode> {
    public static final SimpleBlockNodeRenderer INSTANCE = new SimpleBlockNodeRenderer();

    private SimpleBlockNodeRenderer() {
    }

    @Override
    public void render(@NotNull SimpleClientBlockNode node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        RenderUtils.drawCube(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, 3f / 64f, 3f / 64f, 3f / 64f, node.classHash() | 0xFF000000);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return RenderUtils.distributedEndpoint(nodesAtPos, indexAmongNodes, 1.0 / 8.0, 1.0 / 16.0);
    }
}