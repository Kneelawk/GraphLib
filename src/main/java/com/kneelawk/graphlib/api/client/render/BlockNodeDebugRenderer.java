package com.kneelawk.graphlib.api.client.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.graph.user.debug.DebugBlockNode;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.client.debug.graph.DebugBlockGraph;

/**
 * Handles rendering a {@link DebugBlockNode}.
 *
 * @param <N> the specific type of client block node that this renderer handles.
 */
@Environment(EnvType.CLIENT)
public interface BlockNodeDebugRenderer<N extends DebugBlockNode> {
    /**
     * Renders the client block node.
     * <p>
     * The provided matrix is already translated for rendering relative to the block node's block position.
     *
     * @param node       the client block node to render.
     * @param holderNode the graph-node of the current node, for ease of checking connections and block position.
     * @param consumers  the vertex consumers to render to.
     * @param stack      the matrix stack containing relevant transformations for rendering at the correct position.
     * @param graph      the graph that the node belongs to.
     * @param endpoint   the position returned by {@link #getLineEndpoint(DebugBlockNode, Node, DebugBlockGraph, int, int, List)}.
     * @param graphColor the color that this graph has been assigned.
     */
    void render(@NotNull N node, @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack, @NotNull DebugBlockGraph graph,
                @NotNull Vec3d endpoint, int graphColor);

    /**
     * Gets the point where lines connecting to the given node should be shown connecting to.
     * <p>
     * This is relative to the node's block position.
     *
     * @param node            the node that the lines are connecting to.
     * @param holderNode      the graph node of the current node, for ease of checking connections and block position.
     * @param graph           the graph that the node belongs to.
     * @param nodesAtPos      the number of nodes also sharing the same {@link net.minecraft.util.math.BlockPos} or
     *                        {@link SidedPos}.
     * @param indexAmongNodes the index of this node in the set of nodes being rendered at this position.
     * @param otherEndpoints  a list of all the locations previous nodes at the same position have set as their
     *                        endpoints.
     * @return the position, relative to the node's block position, that lines should be drawn to.
     */
    @NotNull Vec3d getLineEndpoint(@NotNull N node, @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                                   @NotNull DebugBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                   @NotNull List<Vec3d> otherEndpoints);
}
