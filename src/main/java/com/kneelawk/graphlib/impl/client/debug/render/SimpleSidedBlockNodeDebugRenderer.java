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

package com.kneelawk.graphlib.impl.client.debug.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.client.render.BlockNodeDebugRenderer;
import com.kneelawk.graphlib.api.client.render.RenderUtils;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.client.debug.graph.DebugBlockGraph;
import com.kneelawk.graphlib.impl.client.debug.graph.SimpleDebugSidedBlockNode;

public final class SimpleSidedBlockNodeDebugRenderer implements BlockNodeDebugRenderer<SimpleDebugSidedBlockNode> {
    public static final SimpleSidedBlockNodeDebugRenderer INSTANCE = new SimpleSidedBlockNodeDebugRenderer();

    private SimpleSidedBlockNodeDebugRenderer() {
    }

    @Override
    public void render(@NotNull SimpleDebugSidedBlockNode node, @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                       @NotNull VertexConsumerProvider consumers, @NotNull MatrixStack stack,
                       @NotNull DebugBlockGraph graph, @NotNull Vec3d endpoint, int graphColor) {
        RenderUtils.drawRect(stack, consumers.getBuffer(DebugRenderer.Layers.DEBUG_LINES), (float) endpoint.x,
            (float) endpoint.y, (float) endpoint.z, 3f / 64f, 3f / 64f, node.side(), node.color() | 0xFF000000);
    }

    @Override
    public @NotNull Vec3d getLineEndpoint(@NotNull SimpleDebugSidedBlockNode node,
                                          @NotNull Node<ClientBlockNodeHolder, EmptyLinkKey> holderNode,
                                          @NotNull DebugBlockGraph graph, int nodesAtPos, int indexAmongNodes,
                                          @NotNull List<Vec3d> otherEndpoints) {
        return RenderUtils.distributedEndpoint(nodesAtPos, indexAmongNodes, node.side(), 1.0 / 8.0, 1.0 / 8.0,
            1.0 / 32.0);
    }
}
