package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SimpleSidedBlockNodeRenderer implements BlockNodeRenderer<SimpleClientSidedBlockNode> {
    public static final SimpleSidedBlockNodeRenderer INSTANCE = new SimpleSidedBlockNodeRenderer();

    private static final float[][] planarVectors = {
        {1f, 0f, 0f, 0f, 0f, 1f},
        {1f, 0f, 0f, 0f, 1f, 0f},
        {0f, 0f, 1f, 0f, 1f, 0f}
    };

    private SimpleSidedBlockNodeRenderer() {
    }

    @Override
    public void render(@NotNull SimpleClientSidedBlockNode node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        float[] vectors = planarVectors[node.side().ordinal() >> 1];
        DebugRenderer.drawRect(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, vectors[0] / 32f, vectors[1] / 32f, vectors[2] / 32f,
            vectors[3] / 32f, vectors[4] / 32f, vectors[5] / 32f, graphColor);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientSidedBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        Direction side = node.side();
        return new Vec3d(0.5 + side.getOffsetX() * 0.4, 0.5 + side.getOffsetY() * 0.4, 0.5 + side.getOffsetZ() * 0.4);
    }
}
