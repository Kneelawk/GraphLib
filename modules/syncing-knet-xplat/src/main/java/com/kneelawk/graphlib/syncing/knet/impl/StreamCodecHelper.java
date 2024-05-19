package com.kneelawk.graphlib.syncing.knet.impl;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.CodextraStreams;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.util.ObjectType;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

public class StreamCodecHelper {
    public static <S, T extends ObjectType> StreamCodec<FriendlyByteBuf, S> createRefStreamCodec(
        BiFunction<GraphUniverse, ResourceLocation, T> typeGetter, BiFunction<KNetSyncedUniverse, T, S> syncingGetter,
        Function<S, T> syncingToType, String name) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.retrieveWithStreamCodec(GraphLibSyncingKNet.PALETTED_ID_CODEC,
            (universe, id) -> {
                T type = typeGetter.apply(universe.getUniverse(), id);
                if (type == null) throw new DecoderException(
                    name + " type '" + id + "' does not exist in universe '" + universe.getId() + "'");
                S syncing = syncingGetter.apply(universe, type);
                if (syncing == null) throw new DecoderException(
                    name + " type '" + id + "' is not synced in universe '" + universe.getId() + "'");
                return syncing;
            }, (universe, syncing) -> syncingToType.apply(syncing).getId());
    }

    public static <S, T extends ObjectType, O> StreamCodec<NetRegistryByteBuf, O> createObjStreamCodec(
        StreamCodec<? super NetRegistryByteBuf, S> syncingCodec, Function<O, T> getType,
        BiFunction<KNetSyncedUniverse, T, S> getSyncing,
        Function<S, StreamCodec<? super NetRegistryByteBuf, ? extends O>> codecGetter, String name) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.dispatchStreamCodec(
            universe -> CodextraStreams.dispatch(syncingCodec, obj -> {
                S syncing = getSyncing.apply(universe, getType.apply(obj));
                if (syncing == null) throw new EncoderException(
                    name + " type '" + getType.apply(obj).getId() + "' is not synced in universe '" + universe.getId() +
                        "'");
                return syncing;
            }, codecGetter));
    }
}
