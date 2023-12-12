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

package com.kneelawk.graphlib.debugrender.api.graph;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.debugrender.api.client.DebugBlockGraph;
import com.kneelawk.graphlib.debugrender.api.client.render.BlockNodeDebugRenderer;

/**
 * Describes a {@link DebugBlockNode} that is positioned on the side of a block.
 */
public interface SidedDebugBlockNode extends DebugBlockNode {
    /**
     * The side of the block this node is positioned at.
     * <p>
     * The value returned here corresponds to how nodes are grouped together during rendering and influences the
     * <code>nodeCount</code> argument passed to
     * {@link BlockNodeDebugRenderer#getLineEndpoint(DebugBlockNode, Node, DebugBlockGraph, int, int, List)}.
     * <p>
     * A wire is determined to be on the {@link Direction#DOWN} side if it is sitting in the bottom of its block-space,
     * on the top side of the block beneath it. A wire is determined to be on the {@link Direction#NORTH} side if it is
     * sitting at the north side of its block-space, on the south side of the block to the north of it. This same logic
     * applies for all directions.
     *
     * @return the side of the block this node is positioned at.
     */
    @NotNull Direction getSide();
}
