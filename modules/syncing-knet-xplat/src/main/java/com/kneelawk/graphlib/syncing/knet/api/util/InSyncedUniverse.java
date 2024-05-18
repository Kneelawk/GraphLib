package com.kneelawk.graphlib.syncing.knet.api.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.codextra.api.util.FunctionUtils;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;

/**
 * Describes an object in a {@link KNetSyncedUniverse}.
 *
 * @param universe the universe the object is within.
 * @param obj      the object.
 * @param <T>      the type of the object.
 */
public record InSyncedUniverse<T>(KNetSyncedUniverse universe, T obj) {
    /**
     * Creates an in-universe stream codec for the given type.
     * <p>
     * This provides the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.
     *
     * @param objCodec the codec for the object to be associated with a universe.
     * @param <B>      the buffer type.
     * @param <V>      the object type.
     * @return the created stream codec.
     */
    public static <B extends FriendlyByteBuf, V> StreamCodec<B, InSyncedUniverse<V>> codec(
        StreamCodec<? super B, V> objCodec) {
        return StreamCodec.<B, InSyncedUniverse<V>, KNetSyncedUniverse, V>composite(
                KNetSyncedUniverse.ATTACHMENT_KEY.retrieveStream(), FunctionUtils.nullFunc(),
                objCodec, InSyncedUniverse::obj, InSyncedUniverse::new)
            .apply(KNetSyncedUniverse.readAttachingOp(InSyncedUniverse::universe));
    }
}
