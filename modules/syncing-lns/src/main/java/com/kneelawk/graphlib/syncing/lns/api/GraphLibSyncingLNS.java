/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.api;

import alexiil.mc.lib.net.ParentNetIdSingle;

import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.syncing.lns.impl.LNSNetworking;

/**
 * LibNetworkStack-based synchronization library.
 */
public final class GraphLibSyncingLNS {
    private GraphLibSyncingLNS() {}

    /**
     * Net parent for all node entities.
     * <p>
     * This allows node entities to send packets between server-side node entity and client-side node entity.
     */
    public static final ParentNetIdSingle<NodeEntity> NODE_ENTITY_PARENT = LNSNetworking.NODE_ENTITY_PARENT;

    /**
     * Net parent for all link entities.
     * <p>
     * This allows link entities to send packets between server-side link entity and client-side link entity.
     */
    public static final ParentNetIdSingle<LinkEntity> LINK_ENTITY_PARENT = LNSNetworking.LINK_ENTITY_PARENT;

    /**
     * Net parent for all graph entities.
     * <p>
     * This allows graph entities to send packets between server-side graph entity and client-side graph entity.
     */
    @SuppressWarnings("rawtypes")
    public static final ParentNetIdSingle<GraphEntity> GRAPH_ENTITY_PARENT = LNSNetworking.GRAPH_ENTITY_PARENT;
}
