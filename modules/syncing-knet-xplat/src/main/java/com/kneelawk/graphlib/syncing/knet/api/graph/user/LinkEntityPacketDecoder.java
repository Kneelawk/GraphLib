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

package com.kneelawk.graphlib.syncing.knet.api.graph.user;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * Used for decoding {@link LinkEntity}s from packets.
 */
@FunctionalInterface
public interface LinkEntityPacketDecoder {
    /**
     * Decodes a {@link LinkEntity} from a {@link PacketByteBuf}.
     *
     * @param buf    the buffer to read from.
     * @return a newly decoded link entity.
     * @throws PayloadHandlingException if a link entity could not be decoded.
     */
    @NotNull LinkEntity decode(@NotNull PacketByteBuf buf) throws PayloadHandlingException;
}
