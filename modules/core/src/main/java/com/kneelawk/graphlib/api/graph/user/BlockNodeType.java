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

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.ObjectType;

/**
 * Describes a type of block node.
 */
public class BlockNodeType<N extends BlockNode> implements ObjectType {
    private final @NotNull Class<N> clazz;
    private final @NotNull Identifier id;
    private final @NotNull BlockNodeDecoder<N> decoder;

    private BlockNodeType(@NotNull Class<N> clazz, @NotNull Identifier id, @NotNull BlockNodeDecoder<N> decoder) {
        this.clazz = clazz;
        this.id = id;
        this.decoder = decoder;
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
    public @NotNull BlockNodeDecoder<N> getDecoder() {
        return decoder;
    }

    public @NotNull NodeHolder<N> cast(@NotNull NodeHolder<?> holder) {
        return holder.cast(clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockNodeType<?> that = (BlockNodeType<?>) o;

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
     * Creates a new block node type, without packet decoder.
     *
     * @param id      the id of the new type.
     * @param decoder the decoder for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <N extends BlockNode> @NotNull BlockNodeType<N> of(@NotNull Class<N> clazz, @NotNull Identifier id,
                                                                     @NotNull BlockNodeDecoder<N> decoder) {
        return new BlockNodeType<>(clazz, id, decoder);
    }

    /**
     * Creates a new block node type that just invokes a supplier.
     *
     * @param id       the id of the new type.
     * @param supplier a supplier for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <N extends BlockNode> @NotNull BlockNodeType<N> of(@NotNull Class<N> clazz, @NotNull Identifier id,
                                                                     @NotNull Supplier<N> supplier) {
        return new BlockNodeType<>(clazz, id, nbt -> supplier.get());
    }
}
