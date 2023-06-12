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

package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * Describes a type of node entity.
 */
public class NodeEntityType {
    private final @NotNull Identifier id;
    private final @NotNull NodeEntityDecoder decoder;
    private final @Nullable NodeEntityPacketDecoder packetDecoder;

    private NodeEntityType(@NotNull Identifier id, @NotNull NodeEntityDecoder decoder,
                           @Nullable NodeEntityPacketDecoder packetDecoder) {
        this.id = id;
        this.decoder = decoder;
        this.packetDecoder = packetDecoder;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    public @NotNull Identifier getId() {
        return id;
    }

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public @NotNull NodeEntityDecoder getDecoder() {
        return decoder;
    }

    /**
     * Gets this type's packet decoder.
     *
     * @return this type's packet decoder.
     */
    public @Nullable NodeEntityPacketDecoder getPacketDecoder() {
        return packetDecoder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeEntityType that = (NodeEntityType) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "NodeEntityType{" +
            "id=" + id +
            '}';
    }

    /**
     * Creates a new node entity type.
     *
     * @param id            the id of the new node entity type.
     * @param decoder       the decoder for the new node entity type.
     * @param packetDecoder the packet decoder for the new node entity type.
     * @return a new node entity type.
     */
    public static @NotNull NodeEntityType of(@NotNull Identifier id, @NotNull NodeEntityDecoder decoder,
                                             @Nullable NodeEntityPacketDecoder packetDecoder) {
        return new NodeEntityType(id, decoder, packetDecoder);
    }

    /**
     * Creates a new node entity type.
     *
     * @param id      the id of the new node entity type.
     * @param decoder the decoder for the new node entity type.
     * @return a new node entity type.
     */
    public static @NotNull NodeEntityType of(@NotNull Identifier id, @NotNull NodeEntityDecoder decoder) {
        return new NodeEntityType(id, decoder, null);
    }
}
