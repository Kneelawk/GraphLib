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

package com.kneelawk.graphlib.debugrender.api.client;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.graph.Graph;

/**
 * A client-side block graph used for the debug render.
 */
public interface DebugBlockGraph {
    /**
     * This graph's universe's id.
     *
     * @return this graph's universe's id.
     */
    Identifier universeId();

    /**
     * This graph's id.
     *
     * @return this graph's id.
     */
    long graphId();

    /**
     * Gets the graph object containing all the nodes.
     *
     * @return the graph object containing all the nodes.
     */
    Graph<ClientBlockNodeHolder, EmptyLinkKey> graph();

    /**
     * Gets all the chunks this graph is in.
     *
     * @return all the chunks this graph is in.
     */
    LongSet chunks();
}
