/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.impl.graph;

import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;
import com.kneelawk.graphlib.syncing.lns.impl.LNSNetworking;

public final class LNSWorldListener implements WorldListener {
    public static final LNSWorldListener INSTANCE = new LNSWorldListener();

    private LNSWorldListener() {}

    @Override
    public void sendNodeAdd(BlockGraphImpl graph, NodeHolder<BlockNode> node) {
        LNSNetworking.sendNodeAdd(graph, node);
    }

    @Override
    public void sendMerge(BlockGraphImpl from, BlockGraphImpl into) {
        LNSNetworking.sendMerge(from, into);
    }

    @Override
    public void sendLink(BlockGraphImpl graph, LinkHolder<LinkKey> link) {
        LNSNetworking.sendLink(graph, link);
    }

    @Override
    public void sendUnlink(BlockGraphImpl graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {
        LNSNetworking.sendUnlink(graph, a, b, key);
    }

    @Override
    public void sendSplitInto(BlockGraphImpl from, BlockGraphImpl into) {
        LNSNetworking.sendSplitInto(from, into);
    }

    @Override
    public void sendNodeRemove(BlockGraphImpl graph, NodeHolder<BlockNode> holder) {
        LNSNetworking.sendNodeRemove(graph, holder);
    }
}