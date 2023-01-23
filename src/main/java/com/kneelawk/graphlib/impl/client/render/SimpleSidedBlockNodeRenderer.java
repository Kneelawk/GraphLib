package com.kneelawk.graphlib.impl.client.render;

import com.kneelawk.graphlib.api.client.BlockNodeRenderer;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.impl.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.api.graph.struct.Node;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SimpleSidedBlockNodeRenderer implements BlockNodeRenderer<SimpleClientSidedBlockNode> {
    public static final SimpleSidedBlockNodeRenderer INSTANCE = new SimpleSidedBlockNodeRenderer();

    private SimpleSidedBlockNodeRenderer() {
    }

    @Override
    public void render(@NotNull SimpleClientSidedBlockNode node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        RenderUtils.drawRect(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, 3f / 64f, 3f / 64f, node.side(), node.classHash() | 0xFF000000);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientSidedBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return RenderUtils.distributedEndpoint(nodesAtPos, indexAmongNodes, node.side(), 1.0 / 8.0, 1.0 / 8.0,
            1.0 / 32.0);
    }
}
