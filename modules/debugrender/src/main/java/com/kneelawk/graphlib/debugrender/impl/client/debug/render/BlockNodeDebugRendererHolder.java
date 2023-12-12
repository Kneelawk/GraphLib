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

package com.kneelawk.graphlib.debugrender.impl.client.debug.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.debugrender.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.client.render.BlockNodeDebugRenderer;
import com.kneelawk.graphlib.debugrender.api.graph.DebugBlockNode;

/**
 * Holds a {@link BlockNodeDebugRenderer} and its associated class.
 *
 * @param nodeClass    the class of the {@link DebugBlockNode} that the renderer should render.
 * @param nodeRenderer the block node renderer for rendering this type of block node.
 * @param <N>          the type of {@link DebugBlockNode} that the renderer should render.
 */
public record BlockNodeDebugRendererHolder<N extends DebugBlockNode>(@NotNull Class<N> nodeClass,
                                                                     @NotNull BlockNodeDebugRenderer<N> nodeRenderer) {
    public void render(@NotNull DebugBlockNode node, @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull DebugBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        if (nodeClass.isInstance(node)) {
            nodeRenderer.render(nodeClass.cast(node), holderNode, consumers, stack, graph, endpoint, graphColor);
        }
    }

    public Vec3d getLineEndpoint(@NotNull DebugBlockNode node,
                                 @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                                 @NotNull DebugBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                 @NotNull List<Vec3d> otherEndpoints) {
        if (nodeClass.isInstance(node)) {
            return nodeRenderer.getLineEndpoint(nodeClass.cast(node), holderNode, graph, nodesAtPos, indexAmongNodes,
                otherEndpoints);
        } else {
            return Vec3d.ZERO;
        }
    }
}
