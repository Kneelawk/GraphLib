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
import com.kneelawk.graphlib.api.util.HalfLink;

/**
 * Allows an external object to filter the connections connecting to a sided face block node.
 */
public interface SidedFaceConnectionFilter {
    /**
     * Checks whether this filter allows two block nodes to connect.
     *
     * @param self        the node that this check is with respect to.
     * @param holder      the node's holder and context.
     * @param inDirection the direction that the other node is connecting from.
     * @param link        the link to the other block node.
     * @return <code>true</code> if the two block nodes should be allowed to connect, <code>false</code> otherwise.
     */
    boolean canConnect(@NotNull SidedFaceBlockNode self, @NotNull NodeHolder<BlockNode> holder,
                       @NotNull Direction inDirection, @NotNull HalfLink link);

    /**
     * Creates a new connection filter that must satisfy both this filter and the other filter.
     *
     * @param otherFilter the other filter that must be satisfied.
     * @return a new connection filter that must satisfy both this filter and the other filter.
     */
    default @NotNull SidedFaceConnectionFilter and(SidedFaceConnectionFilter otherFilter) {
        return (self, holder, inDirection, link) -> canConnect(self, holder, inDirection, link) &&
            otherFilter.canConnect(self, holder, inDirection, link);
    }
}
