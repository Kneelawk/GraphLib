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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.codec.CodecOrUnit;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.util.ObjectType;

/**
 * Describes a type of link entity.
 */
public final class LinkEntityType implements ObjectType {
    /**
     * {@link BlockNodeType} static codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    public static final Codec<LinkEntityType> REF_CODEC =
        GraphUniverse.ATTACHMENT_KEY.retrieveWithCodecResult(ResourceLocation.CODEC, (universe, id) -> {
            LinkEntityType type = universe.getLinkEntityType(id);
            if (type == null) return DataResult.error(
                () -> "Link entity type '" + id + "' does not exist in universe '" + universe.getId() + "'");
            return DataResult.success(type);
        }, (_universe, type) -> DataResult.success(type.getId()));

    /**
     * {@link LinkEntityType} codec getter.
     *
     * @param universe the universe the link entity types to decode.
     * @return the codec associated with the given universe.
     */
    public static Codec<LinkEntityType> refCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingCodec(universe, REF_CODEC);
    }

    private final @NotNull ResourceLocation id;
    private final @NotNull CodecOrUnit<? extends LinkEntity> codec;

    private LinkEntityType(@NotNull ResourceLocation id, @NotNull CodecOrUnit<? extends LinkEntity> codec) {
        this.id = id;
        this.codec = codec;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public @NotNull CodecOrUnit<? extends LinkEntity> getCodec() {
        return codec;
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
        return "LinkEntityType[" + id + ']';
    }

    /**
     * Creates a new link entity type.
     *
     * @param id    the id of the new type.
     * @param codec the codec for the new type.
     * @return a new link entity type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull LinkEntityType of(@NotNull ResourceLocation id, @NotNull Codec<? extends LinkEntity> codec) {
        return new LinkEntityType(id, CodecOrUnit.codec(codec));
    }

    /**
     * Creates a new link entity type that just invokes a supplier.
     *
     * @param id       the id of the new type.
     * @param supplier a supplier for the new type.
     * @return a new link entity type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull LinkEntityType of(@NotNull ResourceLocation id, @NotNull Supplier<LinkEntity> supplier) {
        return new LinkEntityType(id, CodecOrUnit.unit(supplier));
    }
}
