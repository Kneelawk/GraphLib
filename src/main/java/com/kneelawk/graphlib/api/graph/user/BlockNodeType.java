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

import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.util.ObjectType;

/**
 * Describes a type of block node.
 */
public class BlockNodeType implements ObjectType {
    private final @NotNull Identifier id;
    private final @NotNull BlockNodeDecoder decoder;
    private final @Nullable BlockNodePacketDecoder packetDecoder;

    private BlockNodeType(@NotNull Identifier id, @NotNull BlockNodeDecoder decoder,
                          @Nullable BlockNodePacketDecoder packetDecoder) {
        this.id = id;
        this.decoder = decoder;
        this.packetDecoder = packetDecoder;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    @Override
    public @NotNull Identifier getId() {
        return id;
    }

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public @NotNull BlockNodeDecoder getDecoder() {
        return decoder;
    }

    /**
     * Gets this type's packet decoder.
     *
     * @return this type's packet decoder.
     */
    public @Nullable BlockNodePacketDecoder getPacketDecoder() {
        return packetDecoder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockNodeType that = (BlockNodeType) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "BlockNodeType{" +
            "id=" + id +
            '}';
    }

    /**
     * Creates a new block node type.
     *
     * @param id            the id of the new type.
     * @param decoder       the decoder for the new type.
     * @param packetDecoder the packet decoder for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull BlockNodeType of(@NotNull Identifier id, @NotNull BlockNodeDecoder decoder,
                                            @Nullable BlockNodePacketDecoder packetDecoder) {
        return new BlockNodeType(id, decoder, packetDecoder);
    }

    /**
     * Creates a new block node type, without packet decoder.
     *
     * @param id      the id of the new type.
     * @param decoder the decoder for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull BlockNodeType of(@NotNull Identifier id, @NotNull BlockNodeDecoder decoder) {
        return new BlockNodeType(id, decoder, null);
    }

    /**
     * Creates a new block node type that just invokes a supplier.
     *
     * @param id       the id of the new type.
     * @param supplier a supplier for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull BlockNodeType of(@NotNull Identifier id, @NotNull Supplier<BlockNode> supplier) {
        return new BlockNodeType(id, nbt -> supplier.get(), (buf, ctx) -> supplier.get());
    }
}
