package com.kneelawk.graphlib.impl.client.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.client.render.BlockNodeRenderer;
import com.kneelawk.graphlib.api.client.render.RenderUtils;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientBlockNode;

public final class SimpleBlockNodeRenderer implements BlockNodeRenderer<SimpleClientBlockNode> {
    public static final SimpleBlockNodeRenderer INSTANCE = new SimpleBlockNodeRenderer();

    private SimpleBlockNodeRenderer() {
    }

    @Override
    public void render(@NotNull SimpleClientBlockNode node, @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        RenderUtils.drawCube(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, 3f / 64f, 3f / 64f, 3f / 64f, node.color() | 0xFF000000);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return RenderUtils.distributedEndpoint(nodesAtPos, indexAmongNodes, 1.0 / 8.0, 1.0 / 16.0);
    }
}
