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

package com.kneelawk.graphlib.syncing.knet.api.util;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.knet.api.channel.context.PayloadCodec;

/**
 * A smaller payload representing a {@link LinkPos}, while allowing node data, link key data, and palette data to be
 * stored in a header.
 *
 * @param first  the node payload for the first node.
 * @param second the node payload for the second node.
 * @param typeId the palette'd type id of the link key.
 */
public record LinkPosSmallPayload(@NotNull NodePosSmallPayload first, @NotNull NodePosSmallPayload second,
                                  int typeId) {
    /**
     * This payload's codec.
     */
    public static final PayloadCodec<LinkPosSmallPayload> CODEC = new PayloadCodec<>((buf, payload) -> {
        NodePosSmallPayload.CODEC.encoder().accept(buf, payload.first);
        NodePosSmallPayload.CODEC.encoder().accept(buf, payload.second);
        buf.writeVarUnsignedInt(payload.typeId);
    }, buf -> {
        NodePosSmallPayload first = NodePosSmallPayload.CODEC.decoder().apply(buf);
        NodePosSmallPayload second = NodePosSmallPayload.CODEC.decoder().apply(buf);
        int typeId = buf.readVarUnsignedInt();
        return new LinkPosSmallPayload(first, second, typeId);
    });
}
