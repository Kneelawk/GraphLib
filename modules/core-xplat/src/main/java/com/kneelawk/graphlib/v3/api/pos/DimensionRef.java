package com.kneelawk.graphlib.v3.api.pos;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

/**
 * A reference to a level or dimension type object. This can be a {@link net.minecraft.world.level.Level} or something
 * with a custom implementation like a Create Contraption.
 */
public interface DimensionRef {
    // TODO: Universe lookups
    /**
     * DimensionRef codec.
     */
    MapCodec<DimensionRef> MAP_CODEC = MapCodec.unit(() -> {throw new AssertionError("Stub");});

    /**
     * {@return this dimension ref's type}
     */
    Type<? extends DimensionRef> getType();

    /**
     * The type of a dimension reference.
     *
     * @param id    the id of this type of dimension reference within a given universe.
     * @param codec the codec for encoding and decoding this dimension reference type.
     * @param <T>   the type of dimension reference this type describes.
     */
    record Type<T extends DimensionRef>(ResourceLocation id, MapCodec<T> codec) {}
}
