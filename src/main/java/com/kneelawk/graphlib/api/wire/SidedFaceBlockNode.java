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

package com.kneelawk.graphlib.api.wire;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * A block node that occupies a side of a block and can connect to center-wire block nodes, like a
 * {@link SidedWireBlockNode}, but that cannot connect to other sided block nodes.
 */
public interface SidedFaceBlockNode extends SidedBlockNode {
    /**
     * Checks whether this sided face block node can connect to the given other block node.
     *
     * @param self        this node's holder and context.
     * @param inDirection the direction that the other node is connecting from.
     * @param link        the link to the other block node.
     * @return <code>true</code> if a connection should be allowed to form, <code>false</code> otherwise.
     */
    default boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull Direction inDirection,
                               @NotNull HalfLink link) {
        return true;
    }
}
