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
        DebugRenderer.drawCube(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, 1f / 32f, 1f / 32f, 1f / 32f, graphColor);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return new Vec3d(0.5, 0.5, 0.5);
    }
}
