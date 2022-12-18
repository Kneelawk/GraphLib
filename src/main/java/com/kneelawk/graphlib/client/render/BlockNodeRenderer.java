package com.kneelawk.graphlib.client.render;

import com.kneelawk.graphlib.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles rendering a {@link ClientBlockNode}.
 *
 * @param <N> the specific type of client block node that this renderer handles.
 */
public interface BlockNodeRenderer<N extends ClientBlockNode> {
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
     * @param endpoint   the current line-endpoint of the node.
     * @param graphColor the color that this graph has been assigned.
     * @param hovered
     */
    void render(@NotNull N node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack, @NotNull ClientBlockGraph graph,
                @NotNull Vec3d endpoint, int graphColor, boolean hovered);

    /**
     * Gets the point where lines connecting to the given node should be shown connecting to.
     * <p>
     * This is relative to the node's block position.
     *
     * @param node            the node that the lines are connecting to.
     * @param holderNode      the graph node of the current node, for ease of checking connections and block position.
     * @param graph           the graph that the node belongs to.
     * @param nodesAtPos      the number of nodes also sharing the same {@link net.minecraft.util.math.BlockPos} or
     *                        {@link com.kneelawk.graphlib.util.SidedPos}.
     * @param indexAmongNodes the index of this node in the set of nodes being rendered at this position.
     * @param otherEndpoints  a list of all the locations previous nodes at the same position have set as their
     *                        endpoints.
     * @return the position, relative to the node's block position, that lines should be drawn to.
     */
    @NotNull Vec3d getLineEndpoint(@NotNull N node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                                   @NotNull ClientBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                   @NotNull List<Vec3d> otherEndpoints);

    /**
     * Gets a node's hitbox for selecting the node.
     *
     * @param node       the node to potentially be selected.
     * @param holderNode the graph node of the current node, for ease of checking connections and block position.
     * @param graph      the graph that the node belongs to.
     * @param endpoint   the current line-endpoint of the node.
     * @return a box used for determining whether the player is trying to select the node.
     */
    @NotNull Box getHitbox(@NotNull N node, @NotNull Node<ClientBlockNodeHolder> holderNode,
                           @NotNull ClientBlockGraph graph, @NotNull Vec3d endpoint);

    void renderInspectionData(@NotNull N node, @NotNull Object inspectedData);
}
