package com.kneelawk.graphlib.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.kneelawk.graphlib.api.graph.GraphUniverse;

/**
 * Describes an object in a {@link GraphUniverse}.
 *
 * @param universe the universe the object is within.
 * @param obj      the object.
 * @param <T>      the type of the object.
 */
public record InUniverse<T>(GraphUniverse universe, T obj) {
    /**
     * Creates an in-universe codec for the given type.
     * <p>
     * <b>This provides the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     *
     * @param objCodec the codec for the object to be associated with a universe.
     * @param <T>      the wrapped object type.
     * @return the created codec.
     */
    public static <T> MapCodec<InUniverse<T>> mapCodec(Codec<T> objCodec) {
        return GraphUniverse.ATTACHMENT_KEY.keyAttachingCodec(GraphUniverse.REF_CODEC.fieldOf("universe"),
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                GraphUniverse.ATTACHMENT_KEY.retrieve(),
                objCodec.fieldOf("value").forGetter(InUniverse::obj)
            ).apply(instance, InUniverse::new)), InUniverse::universe);
    }

    public static <T> Codec<InUniverse<T>> codec(Codec<T> objCodec) {
        return mapCodec(objCodec).codec();
    }
}
