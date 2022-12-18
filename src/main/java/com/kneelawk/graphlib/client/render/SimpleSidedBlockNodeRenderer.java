package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.client.graph.SimpleClientSidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
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
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor, boolean hovered) {
        float r = 3f / (hovered ? 32f : 64f);
        RenderUtils.drawRect(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, r, r, node.side(), node.classHash() | 0xFF000000);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleClientSidedBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder> holderNode,
                                          @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return RenderUtils.distributedEndpoint(nodesAtPos, indexAmongNodes, node.side(), 1.0 / 8.0, 1.0 / 8.0,
            1.0 / 32.0);
    }

    @Override
    public @NotNull Box getHitbox(@NotNull SimpleClientSidedBlockNode node,
                                  @NotNull Node<ClientBlockNodeHolder> holderNode, @NotNull ClientBlockGraph graph,
                                  @NotNull Vec3d endpoint) {
        Direction side = node.side();
        float r = 3f / 64f;
        float r2 = 1f / 128f;
        Vec3d v = RenderUtils.perpendicularVector0(side).add(RenderUtils.perpendicularVector1(side));
        Vec3d v2 = RenderUtils.axialVector(side);
        return new Box(endpoint.x - v.x * r - v2.x * r2, endpoint.y - v.y * r - v2.y * r2,
            endpoint.z - v.z * r - v2.z * r2, endpoint.x + v.x * r + v2.x * r2, endpoint.y + v.y * r + v2.y * r2,
            endpoint.z + v.z * r + v2.z * r2);
    }
}
