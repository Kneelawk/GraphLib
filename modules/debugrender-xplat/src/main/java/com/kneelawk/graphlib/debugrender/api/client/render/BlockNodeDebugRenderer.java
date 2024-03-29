/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.debugrender.api.client.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.debugrender.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;

/**
 * Handles rendering a {@link DebugBlockNode}.
 *
 * @param <N> the specific type of client block node that this renderer handles.
 */
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
