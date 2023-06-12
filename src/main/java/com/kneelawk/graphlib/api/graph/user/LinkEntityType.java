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
 * Describes a type of link entity.
 */
public class LinkEntityType {
    private final @NotNull Identifier id;
    private final @NotNull LinkEntityDecoder decoder;
    private final @Nullable LinkEntityPacketDecoder packetDecoder;

    private LinkEntityType(@NotNull Identifier id, @NotNull LinkEntityDecoder decoder,
                           @Nullable LinkEntityPacketDecoder packetDecoder) {
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
    public @NotNull LinkEntityDecoder getDecoder() {
        return decoder;
    }

    /**
     * Gets this type's packet decoder.
     *
     * @return this type's packet decoder.
     */
    public @Nullable LinkEntityPacketDecoder getPacketDecoder() {
        return packetDecoder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkEntityType that = (LinkEntityType) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "LinkEntityType{" +
            "id=" + id +
            '}';
    }

    /**
     * Creates a new link entity type.
     *
     * @param id            the id of the new type.
     * @param decoder       the decoder for the new type.
     * @param packetDecoder the packet decoder for the new type.
     * @return a new link entity type.
     */
    public static @NotNull LinkEntityType of(@NotNull Identifier id, @NotNull LinkEntityDecoder decoder,
                                             @Nullable LinkEntityPacketDecoder packetDecoder) {
        return new LinkEntityType(id, decoder, packetDecoder);
    }

    /**
     * Creates a new link entity type.
     *
     * @param id      the id of the new type.
     * @param decoder the decoder for the new type.
     * @return a new link entity type.
     */
    public static @NotNull LinkEntityType of(@NotNull Identifier id, @NotNull LinkEntityDecoder decoder) {
        return new LinkEntityType(id, decoder, null);
    }
}
