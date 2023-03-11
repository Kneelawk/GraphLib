package com.kneelawk.graphlib.impl.client.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.v1.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.v1.client.render.BlockNodeRenderer;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.v1.util.graph.Node;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;

/**
 * Holds a {@link BlockNodeRenderer} and its associated class.
 *
 * @param nodeClass    the class of the {@link ClientBlockNode} that the renderer should render.
 * @param nodeRenderer the block node renderer for rendering this type of block node.
 * @param <N>          the type of {@link ClientBlockNode} that the renderer should render.
 */
public record BlockNodeRendererHolder<N extends ClientBlockNode>(@NotNull Class<N> nodeClass,
                                                                 @NotNull BlockNodeRenderer<N> nodeRenderer) {
    public void render(@NotNull ClientBlockNode node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        if (nodeClass.isInstance(node)) {
            nodeRenderer.render(nodeClass.cast(node), holderNode, consumers, stack, graph, endpoint, graphColor);
        }
    }

    public Vec3d getLineEndpoint(@NotNull ClientBlockNode node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                                 @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                 @NotNull List<Vec3d> otherEndpoints) {
        if (nodeClass.isInstance(node)) {
            return nodeRenderer.getLineEndpoint(nodeClass.cast(node), holderNode, graph, nodesAtPos, indexAmongNodes,
                otherEndpoints);
        } else {
            return Vec3d.ZERO;
        }
    }
}
