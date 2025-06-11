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

package com.kneelawk.graphlib.v3.api.graph.user;

import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.v3.api.graph.GraphUniverse;
import com.kneelawk.graphlib.v3.api.util.ObjectType;

/**
 * Describes a type of block node.
 */
public final class GraphNodeType implements ObjectType {
    /**
     * {@link GraphNodeType} static codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    public static final Codec<GraphNodeType> REF_CODEC =
        GraphUniverse.ATTACHMENT_KEY.retrieveWithCodecResult(ResourceLocation.CODEC, (universe, id) -> {
            GraphNodeType type = universe.getNodeType(id);
            if (type == null) return DataResult.error(
                () -> "Block node type '" + id + "' does not exist in universe '" + universe.getId() + "'");
            return DataResult.success(type);
        }, (_universe, type) -> DataResult.success(type.getId()));

    /**
     * {@link GraphNodeType} codec getter.
     *
     * @param universe the universe containing the block node types to decode.
     * @return the codec associated with the given universe.
     */
    public static Codec<GraphNodeType> refCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingCodec(universe, REF_CODEC);
    }

    private final ResourceLocation id;
    private final MapCodec<? extends GraphNode> codec;

    private GraphNodeType(ResourceLocation id, MapCodec<? extends GraphNode> codec) {
        this.id = id;
        this.codec = codec;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    @Override
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public MapCodec<? extends GraphNode> getCodec() {
        return codec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphNodeType that = (GraphNodeType) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "BlockNodeType[" + id + ']';
    }

    /**
     * Creates a new block node type, without packet decoder.
     *
     * @param id    the id of the new type.
     * @param codec the codec for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static GraphNodeType of(ResourceLocation id, MapCodec<? extends GraphNode> codec) {
        return new GraphNodeType(id, codec);
    }

    /**
     * Creates a new block node type that just invokes a supplier.
     *
     * @param id       the id of the new type.
     * @param supplier a supplier for the new type.
     * @return a new block node type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static GraphNodeType of(ResourceLocation id, Supplier<GraphNode> supplier) {
        return new GraphNodeType(id, MapCodec.unit(supplier));
    }
}
