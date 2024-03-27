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

package com.kneelawk.graphlib.syncing.knet.impl.payload;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.kneelawk.knet.api.util.NetByteBuf;

public record PayloadGraph(long graphId, int[] graphEntityIds, List<PayloadNode> nodes,
                           List<PayloadInternalLink> internalLinks, List<PayloadExternalLink> externalLinks) {
    public static PayloadGraph decode(NetByteBuf buf) {
        long graphId = buf.readVarUnsignedLong();

        int[] graphEntityIds = PayloadUtils.readVarUnsignedIntArray(buf);

        int nodeCount = buf.readVarUnsignedInt();
        List<PayloadNode> nodes = new ObjectArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(PayloadNode.decode(buf));
        }

        int internalLinkCount = buf.readVarUnsignedInt();
        List<PayloadInternalLink> internalLinks = new ObjectArrayList<>(internalLinkCount);
        for (int i = 0; i < internalLinkCount; i++) {
            internalLinks.add(PayloadInternalLink.decode(buf));
        }

        int externalLinkCount = buf.readVarUnsignedInt();
        List<PayloadExternalLink> externalLinks = new ObjectArrayList<>(externalLinkCount);
        for (int i = 0; i < externalLinkCount; i++) {
            externalLinks.add(PayloadExternalLink.decode(buf));
        }

        return new PayloadGraph(graphId, graphEntityIds, nodes, internalLinks, externalLinks);
    }

    public void encode(NetByteBuf buf) {
        buf.writeVarUnsignedLong(graphId);

        PayloadUtils.writeVarUnsignedIntArray(graphEntityIds, buf);

        buf.writeVarUnsignedInt(nodes.size());
        for (PayloadNode node : nodes) {
            node.encode(buf);
        }

        buf.writeVarUnsignedInt(internalLinks.size());
        for (PayloadInternalLink link : internalLinks) {
            link.encode(buf);
        }

        buf.writeVarUnsignedInt(externalLinks.size());
        for (PayloadExternalLink link : externalLinks) {
            link.encode(buf);
        }
    }
}
